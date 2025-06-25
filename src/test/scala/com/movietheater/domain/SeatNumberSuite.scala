
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SeatNumberSuite extends AnyFunSuite with Matchers {

  test("toString should return the correct string representation") {
    SeatNumber(1).toString should be("1")
    SeatNumber(10).toString should be("10")
  }
}
