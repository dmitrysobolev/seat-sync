
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SeatIdSuite extends AnyFunSuite with Matchers {

  test("toString should return the correct string representation") {
    val seatId = SeatId("A1")
    seatId.toString should be("A1")
  }
}
