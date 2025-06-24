package com.movietheater.json

import com.movietheater.domain.{Movie, MovieId}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import java.time.Duration

object MovieCodecs {
  implicit val encoder: Encoder[Movie] = deriveEncoder[Movie]
  implicit val decoder: Decoder[Movie] = deriveDecoder[Movie]
} 
