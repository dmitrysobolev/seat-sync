package com.movietheater.json

import com.movietheater.domain.{Theater, TheaterId}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object TheaterCodecs {
  implicit val encoder: Encoder[Theater] = deriveEncoder[Theater]
  implicit val decoder: Decoder[Theater] = deriveDecoder[Theater]
} 
