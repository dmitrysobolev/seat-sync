
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID

class AuditoriumIdSuite extends AnyFunSuite with Matchers {

  test("toString should return the correct string representation of the UUID") {
    val uuid = UUID.randomUUID()
    val auditoriumId = AuditoriumId(uuid)
    auditoriumId.toString should be(uuid.toString)
  }
}
