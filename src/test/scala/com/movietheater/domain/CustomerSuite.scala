
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID
import cats.implicits._

class CustomerSuite extends AnyFunSuite with Matchers {

  test("show instance should return the correct string representation") {
    val customer = Customer(
      id = CustomerId(UUID.randomUUID()),
      email = "john.doe@example.com",
      firstName = "John",
      lastName = "Doe"
    )
    customer.show should be("Customer John Doe (john.doe@example.com)")
  }
}
