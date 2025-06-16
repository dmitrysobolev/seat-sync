package com.movietheater.json

import com.movietheater.domain.{AvailableSeatsResponse, ShowtimeId, Seat}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object AvailableSeatsResponseCodecs {
  implicit val encoder: Encoder[AvailableSeatsResponse] = deriveEncoder[AvailableSeatsResponse]
  implicit val decoder: Decoder[AvailableSeatsResponse] = deriveDecoder[AvailableSeatsResponse]
} 