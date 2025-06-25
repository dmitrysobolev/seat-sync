
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID
import cats.implicits._

class AuditoriumSuite extends AnyFunSuite with Matchers {

  test("show instance should return the correct string representation") {
    val auditorium = Auditorium(
      id = AuditoriumId(UUID.randomUUID()),
      theaterId = TheaterId(UUID.randomUUID()),
      name = "Auditorium 1"
    )
    auditorium.show should be("Auditorium Auditorium 1")
  }
}
