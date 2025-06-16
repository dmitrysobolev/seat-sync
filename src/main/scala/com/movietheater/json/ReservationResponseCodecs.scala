package com.movietheater.json

import com.movietheater.domain.{ReservationResponse, Ticket, Money}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object ReservationResponseCodecs {
  implicit val encoder: Encoder[ReservationResponse] = deriveEncoder[ReservationResponse]
  implicit val decoder: Decoder[ReservationResponse] = deriveDecoder[ReservationResponse]
} 