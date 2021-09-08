
import sbt._

object Dependencies {

  object Versions {
    val fs2 = "3.1.1"
    val scalatest = "3.1.1"
    val typesafe = "1.4.1"
  }

  lazy val fs2 = Seq(
    "co.fs2" %% "fs2-core" % Versions.fs2,
    "co.fs2" %% "fs2-io" % Versions.fs2
  )

  lazy val scalatest = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalatest % Test
  )

  lazy val typedconfig = Seq(
    "com.typesafe" % "config" % Versions.typesafe
  )

}