package com.movietheater.json

import com.movietheater.domain.{Seat, SeatId, TheaterId, AuditoriumId, RowNumber, SeatNumber}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object SeatCodecs {
  implicit val encoder: Encoder[Seat] = deriveEncoder[Seat]
  implicit val decoder: Decoder[Seat] = deriveDecoder[Seat]
} 
