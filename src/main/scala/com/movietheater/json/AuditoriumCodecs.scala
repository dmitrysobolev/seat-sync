package com.movietheater.json

import com.movietheater.domain.{Auditorium, AuditoriumId, TheaterId}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object AuditoriumCodecs {
  implicit val encoder: Encoder[Auditorium] = deriveEncoder[Auditorium]
  implicit val decoder: Decoder[Auditorium] = deriveDecoder[Auditorium]
} 
