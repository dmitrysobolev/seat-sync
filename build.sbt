ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.1"

lazy val root = (project in file("."))
  .settings(
    name := "seat-sync",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-effect" % "3.5.2",
      "org.http4s" %% "http4s-ember-server" % "0.23.24",
      "org.http4s" %% "http4s-ember-client" % "0.23.24",
      "org.http4s" %% "http4s-circe" % "0.23.24",
      "org.http4s" %% "http4s-dsl" % "0.23.24",
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7",
      "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.7",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-h2" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test
    )
  ) 