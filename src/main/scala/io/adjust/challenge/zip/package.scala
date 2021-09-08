package io.adjust.challenge

import cats.data.OptionT
import cats.effect._
import cats.effect.std.Dispatcher
import cats.implicits._
import fs2.{Pipe, Stream, io}
import fs2.io.file

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import java.io.InputStream
import java.util.zip._

package object zip {
  def unzipP[F[_]](
      chunkSize: Int
  )(implicit F: Sync[F], con: Async[F]): Pipe[F, Byte, (String, Stream[F, Byte])] = {

    def entry(zis: ZipInputStream): OptionT[F, (String, Stream[F, Byte])] =
      OptionT(Sync[F].delay(Option(zis.getNextEntry()))).map { ze =>
        (ze.getName, io.readInputStream[F](F.delay(zis), chunkSize, false))
      }

    def unzipEntries(zis: ZipInputStream): Stream[F, (String, Stream[F, Byte])] =
      Stream.unfoldEval(zis) { zis0 => entry(zis0).map((_, zis0)).value }

    value: Stream[F, Byte] =>
      value.through(io.toInputStream).flatMap { is: InputStream =>
        val zis: F[ZipInputStream]          = Sync[F].delay(new ZipInputStream(is))
        val zres: Stream[F, ZipInputStream] = Stream.bracket(zis)(zis => Sync[F].delay(zis.close()))
        zres.flatMap { z => unzipEntries(z) }
      }
  }
  def unzip[F[_]](zipped: Stream[F, Byte], chunkSize: Int)(
      implicit F: Sync[F],
      con: Async[F]
  ): Stream[F, (String, Stream[F, Byte])] =
    zipped.through(unzipP(chunkSize))

}
