
import sbt._

object Dependencies {

  object Versions {
    val fs2 = "3.1.1"
    val scalatest = "3.1.1"
    val typesafe = "1.4.1"
    val postgres = "42.2.23"
    val logback = "1.2.5"
    val logstash = "6.6"
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

  lazy val postgres = Seq(
    "org.postgresql" % "postgresql" % Versions.postgres
  )

  lazy val logger = Seq(
    "ch.qos.logback" % "logback-core" % Versions.logback,
  "ch.qos.logback" % "logback-classic" % Versions.logback,
  "net.logstash.logback" % "logstash-logback-encoder" % Versions.logstash
  )

}