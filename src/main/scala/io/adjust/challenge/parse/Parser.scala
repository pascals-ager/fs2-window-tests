package io.adjust.challenge.parse

import cats.effect.{ExitCode, IO, IOApp}
import fs2.io.file.{Files, Path}
import io.adjust.challenge.zip._
import cats.implicits._
import io.adjust.challenge.domain.HeaderRecord
import io.adjust.challenge.domain.Transform.{lsplit, transform}
object Parser extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Files[IO]
      .walk(Path("/home/pascals/coding-challenges/adjust/data"))
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
      //.evalTap { e => IO.delay(println(e)) }
      .through(transform[IO]((List[String](""), 0)))
      .evalTap { e => IO.delay(println(e)) }
      .compile
      .drain
      .as(ExitCode.Success)
}
