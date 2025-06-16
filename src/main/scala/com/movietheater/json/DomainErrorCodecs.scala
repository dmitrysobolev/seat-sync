package com.movietheater.json

import com.movietheater.domain.DomainError
import io.circe.{Decoder, Encoder}

object DomainErrorCodecs {
  implicit val encoder: Encoder[DomainError] = Encoder.instance { error =>
    io.circe.Json.obj(
      "error" -> io.circe.Json.fromString(error.getClass.getSimpleName),
      "message" -> io.circe.Json.fromString(error.message)
    )
  }
} 