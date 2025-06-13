package com.movietheater.config

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AppConfigSpec extends AnyWordSpec with Matchers {

  "ServerConfig" should {
    "be created with host and port" in {
      val config = ServerConfig("localhost", 8080)
      
      config.host shouldBe "localhost"
      config.port shouldBe 8080
    }
    
    "be created with different values" in {
      val config = ServerConfig("0.0.0.0", 9000)
      
      config.host shouldBe "0.0.0.0"
      config.port shouldBe 9000
    }
    
    "support copy method" in {
      val original = ServerConfig("localhost", 8080)
      val modified = original.copy(port = 9090)
      
      modified.host shouldBe "localhost"
      modified.port shouldBe 9090
    }
  }

  "DatabaseConfig" should {
    "be created with all required fields" in {
      val config = DatabaseConfig(
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost:5432/movietheater",
        user = "postgres",
        password = "postgres"
      )
      
      config.driver shouldBe "org.postgresql.Driver"
      config.url shouldBe "jdbc:postgresql://localhost:5432/movietheater"
      config.user shouldBe "postgres"
      config.password shouldBe "postgres"
      config.poolSize shouldBe 10 // default value
    }
    
    "support custom pool size" in {
      val config = DatabaseConfig(
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost:5432/movietheater",
        user = "postgres",
        password = "postgres",
        poolSize = 20
      )
      
      config.poolSize shouldBe 20
    }
  }

  "AppConfig" should {
    "be created with server and database config" in {
      val serverConfig = ServerConfig("localhost", 8080)
      val databaseConfig = DatabaseConfig(
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost:5432/movietheater",
        user = "postgres",
        password = "postgres"
      )
      val appConfig = AppConfig(serverConfig, databaseConfig)
      
      appConfig.server shouldBe serverConfig
      appConfig.database shouldBe databaseConfig
    }
    
    "support nested configuration" in {
      val appConfig = AppConfig(
        server = ServerConfig("0.0.0.0", 3000),
        database = DatabaseConfig(
          driver = "org.postgresql.Driver",
          url = "jdbc:postgresql://localhost:5432/movietheater",
          user = "postgres",
          password = "postgres"
        )
      )
      
      appConfig.server.host shouldBe "0.0.0.0"
      appConfig.server.port shouldBe 3000
      appConfig.database.driver shouldBe "org.postgresql.Driver"
    }
    
    "support copy method" in {
      val original = AppConfig(
        ServerConfig("localhost", 8080),
        DatabaseConfig(
          driver = "org.postgresql.Driver",
          url = "jdbc:postgresql://localhost:5432/movietheater",
          user = "postgres",
          password = "postgres"
        )
      )
      val modified = original.copy(server = ServerConfig("0.0.0.0", 9000))
      
      modified.server.host shouldBe "0.0.0.0"
      modified.server.port shouldBe 9000
      modified.database shouldBe original.database
    }
  }
} 