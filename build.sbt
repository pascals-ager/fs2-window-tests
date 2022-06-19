import Dependencies._
import com.typesafe.sbt.packager.docker.Cmd

lazy val commonSettings = Seq(
  name := "parser-win-stream",
  scalaVersion := "2.13.4",
  version := "0.1",
  organization := "io.win.stream",
  scalacOptions ++= Seq(
    // warnings
    "-unchecked", // able additional warnings where generated code depends on assumptions
    "-deprecation", // emit warning for usages of deprecated APIs
    "-feature", // emit warning usages of features that should be imported explicitly
    // Features enabled by default
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    // possibly deprecated optionsyes
    "-Ywarn-dead-code",
    "-language:higherKinds",
    "-language:existentials",
    "-Ywarn-extra-implicit"
  )
)

resolvers ++= Seq(
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases",
  Resolver.sonatypeRepo("releases")
)


lazy val parser = (project in file(".")).
  enablePlugins(
    JavaServerAppPackaging,
    AshScriptPlugin,
    DockerPlugin).
  settings(moduleName := "parser").
  settings(mainClass in Compile := Some("io.win.stream.parse.Parser")).

  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= fs2 ++ postgres ++ logger
  )

scalafmtOnCompile := true

/* Default the image is built on openjdk11 */
dockerBaseImage := "adoptopenjdk/openjdk11"
daemonUser in Docker    := "parser"
