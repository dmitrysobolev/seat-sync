package com.movietheater.json

import com.movietheater.domain.{CreateReservationRequest, ShowtimeId, CustomerId, SeatId}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object CreateReservationRequestCodecs {
  implicit val encoder: Encoder[CreateReservationRequest] = deriveEncoder[CreateReservationRequest]
  implicit val decoder: Decoder[CreateReservationRequest] = deriveDecoder[CreateReservationRequest]
} 