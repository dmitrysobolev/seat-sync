package com.movietheater.config

case class AppConfig(
  server: ServerConfig
)

case class ServerConfig(
  host: String,
  port: Int
) 