package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.LocalDateTime

case class Seat(
  id: SeatId,
  theaterId: TheaterId,
  auditoriumId: AuditoriumId,
  rowNumber: RowNumber,
  seatNumber: SeatNumber,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
) {
  def row: String = rowNumber.value.toString
  def number: Int = seatNumber.value
}

object Seat {
  implicit val show: Show[Seat] = Show.show(seat => 
    s"Seat ${seat.rowNumber}${seat.seatNumber}"
  )
} 