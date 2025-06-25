
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID
import cats.implicits._

class TheaterSuite extends AnyFunSuite with Matchers {

  test("show instance should return the correct string representation") {
    val theater = Theater(
      id = TheaterId(UUID.randomUUID()),
      name = "The Grand Cinema",
      address = "123 Main St",
      totalSeats = 500
    )
    theater.show should be("Theater The Grand Cinema (123 Main St)")
  }
}
