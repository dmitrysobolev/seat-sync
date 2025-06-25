
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID
import java.time.LocalDateTime

class ShowtimeSuite extends AnyFunSuite with Matchers {

  test("getPriceForSeat should return the correct price for a given seat") {
    val seatId = SeatId("A1")
    val showtime = Showtime(
      id = ShowtimeId(UUID.randomUUID()),
      movieId = MovieId(UUID.randomUUID()),
      theaterId = TheaterId(UUID.randomUUID()),
      auditoriumId = AuditoriumId(UUID.randomUUID()),
      startTime = LocalDateTime.now(),
      seatTypes = Map(seatId -> SeatType.Standard),
      seatPrices = Map(SeatType.Standard -> Money(1000)),
      seatStatus = Map(seatId -> SeatStatus.Available)
    )

    showtime.getPriceForSeat(seatId) should be(Some(Money(1000)))
  }

  test("getPriceForSeat should return None for a seat that doesn't exist") {
    val seatId = SeatId("A1")
    val showtime = Showtime(
      id = ShowtimeId(UUID.randomUUID()),
      movieId = MovieId(UUID.randomUUID()),
      theaterId = TheaterId(UUID.randomUUID()),
      auditoriumId = AuditoriumId(UUID.randomUUID()),
      startTime = LocalDateTime.now(),
      seatTypes = Map(),
      seatPrices = Map(SeatType.Standard -> Money(1000)),
      seatStatus = Map()
    )

    showtime.getPriceForSeat(seatId) should be(None)
  }
}
