package io.adjust.challenge.utils

import cats.data.OptionT
import cats.effect._
import cats.effect.std.Dispatcher
import cats.implicits._
import fs2.{Pipe, Stream, io}
import fs2.io.file

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import java.io.InputStream
import java.util.zip._

object UnzipUtils {
  def unzipP[F[_]](
      chunkSize: Int
  )(implicit F: Async[F]): Pipe[F, Byte, (String, Stream[F, Byte])] = {

    def entry(zis: ZipInputStream): OptionT[F, (String, Stream[F, Byte])] =
      OptionT(Async[F].delay(Option(zis.getNextEntry()))).map { ze =>
        (ze.getName, io.readInputStream[F](F.delay(zis), chunkSize, false))
      }

    def unzipEntries(zis: ZipInputStream): Stream[F, (String, Stream[F, Byte])] =
      Stream.unfoldEval(zis) { zis0 => entry(zis0).map((_, zis0)).value }

    value: Stream[F, Byte] =>
      value.through(io.toInputStream).flatMap { is: InputStream =>
        val zis: F[ZipInputStream]          = Async[F].delay(new ZipInputStream(is))
        val zres: Stream[F, ZipInputStream] = Stream.bracket(zis)(zis => Async[F].delay(zis.close()))
        zres.flatMap { z => unzipEntries(z) }
      }
  }
  def unzip[F[_]](zipped: Stream[F, Byte], chunkSize: Int)(
      implicit F: Async[F]
  ): Stream[F, (String, Stream[F, Byte])] =
    zipped.through(unzipP(chunkSize))

}
