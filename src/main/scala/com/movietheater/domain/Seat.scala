package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime
import java.util.UUID

case class Seat(
  id: SeatId,
  showtimeId: ShowtimeId,
  rowNumber: RowNumber,
  seatNumber: SeatNumber,
  seatType: SeatType
)

object Seat {
  implicit val show: Show[Seat] = Show.show(seat => 
    s"Seat ${seat.rowNumber}${seat.seatNumber} (${seat.seatType})"
  )
  
  implicit val encoder: Encoder[Seat] = Encoder.forProduct5(
    "id", "showtimeId", "rowNumber", "seatNumber", "seatType"
  )(seat => (
    seat.id,
    seat.showtimeId,
    seat.rowNumber,
    seat.seatNumber,
    seat.seatType
  ))
  
  implicit val decoder: Decoder[Seat] = Decoder.forProduct5(
    "id", "showtimeId", "rowNumber", "seatNumber", "seatType"
  )(Seat.apply)
  
  implicit val get: Get[Seat] = Get[(SeatId, ShowtimeId, String, Int, String)].map {
    case (id, showtimeId, rowNumber, seatNumber, seatType) =>
      Seat(
        id = id,
        showtimeId = showtimeId,
        rowNumber = RowNumber(rowNumber.head),
        seatNumber = SeatNumber(seatNumber),
        seatType = SeatType.fromString(seatType).getOrElse(SeatType.Standard)
      )
  }
  
  implicit val put: Put[Seat] = Put[(SeatId, ShowtimeId, String, Int, String)].contramap { seat =>
    (
      seat.id,
      seat.showtimeId,
      seat.rowNumber.toString,
      seat.seatNumber.value,
      seat.seatType.toString
    )
  }
} 