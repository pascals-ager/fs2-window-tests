package io.win.stream.utils

import cats.{ApplicativeError, MonadError, MonadThrow}
import cats.effect.kernel.Resource.ExitCase
import cats.effect.{Async, IO, Resource, Sync}
import fs2.{Chunk, Pull, Stream}
import io.win.stream.domain.NormalizedRecord
import io.win.stream.parse.Parser.getClass
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.{Logger, LoggerFactory}

import java.sql.{Connection, DriverManager, PreparedStatement}
import scala.util.Try

trait PostgresSink[F[_]] {
  def writeChunks(in: Chunk[Chunk[NormalizedRecord]]): F[Unit]
}

object PostgresSink {
  val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  def create[F[_]](implicit F: Sync[F]): Resource[F, PostgresSink[F]] = {

    val open: F[Connection] = F.delay {
      val ds = new PGSimpleDataSource()
      ds.setServerNames(Array("postgres-win"))
      ds.setPortNumbers(Array(5432))
      ds.setDatabaseName("postgres")
      ds.setUser("postgres")
      ds.setPassword("postgres")
      val conn: Connection = ds.getConnection()
      conn
    }

    Resource
      .makeCase(open) {
        case (con, ExitCase.Succeeded | ExitCase.Canceled) =>
          F.delay {
            con.close()
          }
        case (con, ExitCase.Errored(_)) =>
          F.delay {
            con.close()
          }
      }
      .map { postgresConn =>
        new PostgresSink[F] {
          Class.forName("org.postgresql.Driver")
          override def writeChunks(in: Chunk[Chunk[NormalizedRecord]]): F[Unit] = F.delay {
            in.foreach { chunk =>
              postgresConn.beginRequest()
              val statement: PreparedStatement = postgresConn.prepareStatement(NormalizedRecord.normalizedInsertSql)
              chunk.toList.foreach { rec =>
                statement.setString(1, rec.record)
                statement.setString(2, rec.id)
                statement.setInt(3, rec.year)
                statement.setInt(4, rec.month)
                statement.setInt(5, rec.day)
                statement.setInt(6, rec.hour)
                statement.setInt(7, rec.reltime)
                statement.setInt(8, rec.numlev)
                statement.setString(9, rec.p_src)
                statement.setString(10, rec.np_src)
                statement.setInt(11, rec.lat)
                statement.setInt(12, rec.long)
                statement.setInt(13, rec.lvltypone)
                statement.setInt(14, rec.lvltyptwo)
                statement.setInt(15, rec.etime)
                statement.setInt(16, rec.press)
                statement.setString(17, rec.pflag)
                statement.setInt(18, rec.gph)
                statement.setString(19, rec.zflag)
                statement.setInt(20, rec.temp)
                statement.setString(21, rec.tflag)
                statement.setInt(22, rec.rh)
                statement.setInt(23, rec.dpdp)
                statement.setInt(24, rec.wdir)
                statement.setInt(25, rec.wspd)
                statement.addBatch()
              }
              statement.executeBatch()
            }
          }
        }
      }

  }

  def writeStream: Stream[IO, Chunk[Chunk[NormalizedRecord]]] => Stream[IO, Stream[IO, Unit]] =
    in =>
      in.repeatPull {
        _.uncons.flatMap {
          case Some((hd: Chunk[Chunk[Chunk[NormalizedRecord]]], tl: Stream[IO, Chunk[Chunk[NormalizedRecord]]])) =>
            Pull
              .output(
                hd.map { chunk =>
                  Stream
                    .resource(PostgresSink.create[IO])
                    .flatMap { postgresSink =>
                      val write: IO[Unit] = for {
                        _ <- postgresSink.writeChunks(chunk)
                      } yield ()
                      Stream.eval(write)
                    }
                }
              )
              .as(Some(tl))
          case None => Pull.pure(None)
        }
      }
}
