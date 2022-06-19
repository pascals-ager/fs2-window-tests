package io.win.stream.parse

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2.Stream
import fs2.io.file.{Files, Path}
import io.win.stream.utils.UnzipUtils._
import io.win.stream.domain.Normalize._
import io.win.stream.domain.NormalizedRecord
import io.win.stream.utils.PostgresSink._
import io.win.stream.utils.{PostgresSink, Transform}
import org.slf4j.{Logger, LoggerFactory}

object Parser extends IOApp {
  implicit val logger: Logger = LoggerFactory.getLogger(getClass.getName)
  override def run(args: List[String]): IO[ExitCode] = {
    val postgresStream: Stream[IO, Unit] = Files[IO]
      .walk(Path("/tmp/adjust/data"))
      .evalTap(e => IO.delay(println(e)))
      .filter(_.extName == ".zip")
      .flatMap { zippedFile =>
        Files[IO]
          .readAll(zippedFile)
          .through(unzip[IO](_, 4096))
          .flatMap { case (name, stream) => stream }
          .through(fs2.text.utf8.decode)
          .through(fs2.text.lines)
      }
      .through(normalize[IO]((List[String](""), 0)))
      .filter(list => list.nonEmpty)
      .through(Transform[IO, List[String], NormalizedRecord])
      .chunkN(10000)
      .chunkN(10)
      .through(writeStream)
      .parJoinUnbounded

    //
    postgresStream
      .handleErrorWith {
        case sql: java.sql.SQLException =>
          Stream.eval(IO.delay(logger.error(s"Exception Occurred - ${sql.getMessage}")))

        case num: java.lang.NumberFormatException =>
          Stream.eval(IO.delay(logger.warn(s"Exception Occurred - ${num.getMessage}")))
      }
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
