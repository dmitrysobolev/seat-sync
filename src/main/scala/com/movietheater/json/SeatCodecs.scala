package com.movietheater.json

import com.movietheater.domain.{Seat, SeatId, TheaterId, AuditoriumId, RowNumber, SeatNumber}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

object SeatCodecs {
  implicit val encoder: Encoder[Seat] = Encoder.forProduct7(
    "id", "theaterId", "auditoriumId", "rowNumber", "seatNumber", "createdAt", "updatedAt"
  )(seat => (
    seat.id,
    seat.theaterId,
    seat.auditoriumId,
    seat.rowNumber,
    seat.seatNumber,
    seat.createdAt,
    seat.updatedAt
  ))
  
  implicit val decoder: Decoder[Seat] = Decoder.forProduct7(
    "id", "theaterId", "auditoriumId", "rowNumber", "seatNumber", "createdAt", "updatedAt"
  )(Seat.apply)
} 