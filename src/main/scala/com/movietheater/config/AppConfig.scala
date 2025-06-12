package com.movietheater.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._

case class AppConfig(
  server: ServerConfig,
  database: DatabaseConfig
) derives ConfigReader

case class ServerConfig(
  host: String,
  port: Int
) derives ConfigReader

case class DatabaseConfig(
  driver: String,
  url: String,
  user: String,
  password: String,
  poolSize: Int = 10
) derives ConfigReader 