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

  "AppConfig" should {
    "be created with server config" in {
      val serverConfig = ServerConfig("localhost", 8080)
      val appConfig = AppConfig(serverConfig)
      
      appConfig.server shouldBe serverConfig
    }
    
    "support nested configuration" in {
      val appConfig = AppConfig(
        server = ServerConfig("0.0.0.0", 3000)
      )
      
      appConfig.server.host shouldBe "0.0.0.0"
      appConfig.server.port shouldBe 3000
    }
    
    "support copy method" in {
      val original = AppConfig(ServerConfig("localhost", 8080))
      val modified = original.copy(server = ServerConfig("0.0.0.0", 9000))
      
      modified.server.host shouldBe "0.0.0.0"
      modified.server.port shouldBe 9000
    }
  }
} 