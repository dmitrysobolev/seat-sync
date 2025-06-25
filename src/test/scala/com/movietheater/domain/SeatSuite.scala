
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID

class SeatSuite extends AnyFunSuite with Matchers {

  test("row should return the correct row number") {
    val seat = Seat(
      id = SeatId("A1"),
      theaterId = TheaterId(UUID.randomUUID()),
      auditoriumId = AuditoriumId(UUID.randomUUID()),
      rowNumber = RowNumber('A'),
      seatNumber = SeatNumber(1)
    )
    seat.row should be("A")
  }

  test("number should return the correct seat number") {
    val seat = Seat(
      id = SeatId("A1"),
      theaterId = TheaterId(UUID.randomUUID()),
      auditoriumId = AuditoriumId(UUID.randomUUID()),
      rowNumber = RowNumber('A'),
      seatNumber = SeatNumber(1)
    )
    seat.number should be(1)
  }
}
