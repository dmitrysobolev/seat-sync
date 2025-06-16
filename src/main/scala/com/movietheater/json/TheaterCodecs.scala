package com.movietheater.json

import com.movietheater.domain.{Theater, TheaterId}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

object TheaterCodecs {
  implicit val encoder: Encoder[Theater] = Encoder.forProduct6(
    "id", "name", "address", "totalSeats", "createdAt", "updatedAt"
  )(theater => (
    theater.id,
    theater.name,
    theater.address,
    theater.totalSeats,
    theater.createdAt,
    theater.updatedAt
  ))
  
  implicit val decoder: Decoder[Theater] = Decoder.forProduct6(
    "id", "name", "address", "totalSeats", "createdAt", "updatedAt"
  )(Theater.apply)
} 