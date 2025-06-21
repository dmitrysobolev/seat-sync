package com.movietheater.json

import com.movietheater.domain.{Theater, TheaterId}
import io.circe.{Decoder, Encoder}

object TheaterCodecs {
  implicit val encoder: Encoder[Theater] = Encoder.forProduct4(
    "id", "name", "address", "totalSeats"
  )(theater => (
    theater.id,
    theater.name,
    theater.address,
    theater.totalSeats
  ))
  
  implicit val decoder: Decoder[Theater] = Decoder.forProduct4(
    "id", "name", "address", "totalSeats"
  )(Theater.apply)
} 