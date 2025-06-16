package com.movietheater.json

import com.movietheater.domain.{Auditorium, AuditoriumId, TheaterId}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

object AuditoriumCodecs {
  implicit val encoder: Encoder[Auditorium] = Encoder.forProduct5(
    "id", "theaterId", "name", "createdAt", "updatedAt"
  )(auditorium => (
    auditorium.id,
    auditorium.theaterId,
    auditorium.name,
    auditorium.createdAt,
    auditorium.updatedAt
  ))
  
  implicit val decoder: Decoder[Auditorium] = Decoder.forProduct5(
    "id", "theaterId", "name", "createdAt", "updatedAt"
  )(Auditorium.apply)
} 