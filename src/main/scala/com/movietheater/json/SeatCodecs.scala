package com.movietheater.json

import com.movietheater.domain.{Seat, SeatId, TheaterId, AuditoriumId, RowNumber, SeatNumber}
import io.circe.{Decoder, Encoder}

object SeatCodecs {
  implicit val encoder: Encoder[Seat] = Encoder.forProduct5(
    "id", "theaterId", "auditoriumId", "rowNumber", "seatNumber"
  )(seat => (
    seat.id,
    seat.theaterId,
    seat.auditoriumId,
    seat.rowNumber,
    seat.seatNumber
  ))
  
  implicit val decoder: Decoder[Seat] = Decoder.forProduct5(
    "id", "theaterId", "auditoriumId", "rowNumber", "seatNumber"
  )(Seat.apply)
} 