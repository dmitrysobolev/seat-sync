
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SeatTypeSuite extends AnyFunSuite with Matchers {

  test("fromString should correctly parse 'standard'") {
    SeatType.fromString("standard") should be(Some(SeatType.Standard))
  }

  test("fromString should correctly parse 'premium'") {
    SeatType.fromString("premium") should be(Some(SeatType.Premium))
  }

  test("fromString should correctly parse 'vip'") {
    SeatType.fromString("vip") should be(Some(SeatType.VIP))
  }

  test("fromString should return None for an invalid string") {
    SeatType.fromString("invalid") should be(None)
  }

  test("toString should return 'standard' for Standard type") {
    SeatType.Standard.toString should be("standard")
  }

  test("toString should return 'premium' for Premium type") {
    SeatType.Premium.toString should be("premium")
  }

  test("toString should return 'vip' for VIP type") {
    SeatType.VIP.toString should be("vip")
  }
}
