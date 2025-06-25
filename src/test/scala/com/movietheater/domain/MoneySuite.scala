
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class MoneySuite extends AnyFunSuite with Matchers {

  test("Money should be created with a positive value in cents") {
    val money = Money(1000) // 10.00
    money.cents should be(1000L)
  }

  test("fromDollars should correctly create Money") {
    val money = Money.fromDollars(10, 50) // $10.50
    money.cents should be(1050L)
  }

  test("toString should format money correctly") {
    val money = Money.fromDollars(12, 75)
    money.toString should be("$12.75")
  }

  test("addition should work correctly") {
    val m1 = Money(100)
    val m2 = Money(50)
    (m1 + m2).cents should be(150L)
  }

  test("subtraction should work correctly") {
    val m1 = Money(100)
    val m2 = Money(50)
    (m1 - m2).cents should be(50L)
  }
}
