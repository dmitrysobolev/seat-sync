package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.LocalDateTime

case class Showtime(
  id: ShowtimeId,
  movieId: MovieId,
  theaterId: TheaterId,
  auditoriumId: AuditoriumId,
  startTime: LocalDateTime,
  seatTypes: Map[SeatId, SeatType],
  seatPrices: Map[SeatType, Money],
  seatStatus: Map[SeatId, SeatStatus],
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
) {
  def getPriceForSeat(seatId: SeatId): Option[Money] = {
    seatTypes.get(seatId).flatMap(seatType => seatPrices.get(seatType))
  }
}

object Showtime {
  implicit val show: Show[Showtime] = Show.show(showtime => 
    s"Showtime at ${showtime.startTime}"
  )
} 