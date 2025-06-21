package com.movietheater.json

import com.movietheater.domain.{Auditorium, AuditoriumId, TheaterId}
import io.circe.{Decoder, Encoder}

object AuditoriumCodecs {
  implicit val encoder: Encoder[Auditorium] = Encoder.forProduct3(
    "id", "theaterId", "name"
  )(auditorium => (
    auditorium.id,
    auditorium.theaterId,
    auditorium.name
  ))
  
  implicit val decoder: Decoder[Auditorium] = Decoder.forProduct3(
    "id", "theaterId", "name"
  )(Auditorium.apply)
} 