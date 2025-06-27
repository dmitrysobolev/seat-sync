ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.1"

lazy val root = (project in file("."))
  .settings(
    name := "seat-sync",
    libraryDependencies ++= Seq(
      // Typelevel dependencies
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.6.1",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0",
      "org.typelevel" %% "log4cats-core" % "2.6.0",

      // HTTP4s dependencies
      "org.http4s" %% "http4s-ember-server" % "0.23.24",
      "org.http4s" %% "http4s-ember-client" % "0.23.24",
      "org.http4s" %% "http4s-circe" % "0.23.24",
      "org.http4s" %% "http4s-dsl" % "0.23.24",

      // JSON dependencies
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",

      // Configuration
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7",
      "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.7",

      // Database dependencies
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC4",
      "org.tpolecat" %% "doobie-h2" % "1.0.0-RC4",
      "org.postgresql" % "postgresql" % "42.7.2",
      "org.flywaydb" % "flyway-core" % "10.0.1",

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.4.11",

      // Testing dependencies
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC4" % Test,
      "org.testcontainers" % "postgresql" % "1.19.3" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test
    )
  ) 