
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class SeatStatusSuite extends AnyFunSuite with Matchers {

  test("fromString should correctly parse 'available'") {
    SeatStatus.fromString("available") should be(Some(SeatStatus.Available))
  }

  test("fromString should correctly parse 'reserved'") {
    SeatStatus.fromString("reserved") should be(Some(SeatStatus.Reserved))
  }

  test("fromString should correctly parse 'sold'") {
    SeatStatus.fromString("sold") should be(Some(SeatStatus.Sold))
  }

  test("fromString should return None for an invalid string") {
    SeatStatus.fromString("invalid") should be(None)
  }

  test("toString should return 'available' for Available status") {
    SeatStatus.Available.toString should be("available")
  }

  test("toString should return 'reserved' for Reserved status") {
    SeatStatus.Reserved.toString should be("reserved")
  }

  test("toString should return 'sold' for Sold status") {
    SeatStatus.Sold.toString should be("sold")
  }
}
