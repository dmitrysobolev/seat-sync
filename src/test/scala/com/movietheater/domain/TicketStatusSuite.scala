
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TicketStatusSuite extends AnyFunSuite with Matchers {

  test("fromString should correctly parse 'reserved'") {
    TicketStatus.fromString("reserved") should be(Some(TicketStatus.Reserved))
  }

  test("fromString should correctly parse 'purchased'") {
    TicketStatus.fromString("purchased") should be(Some(TicketStatus.Purchased))
  }

  test("fromString should correctly parse 'cancelled'") {
    TicketStatus.fromString("cancelled") should be(Some(TicketStatus.Cancelled))
  }

  test("fromString should return None for an invalid string") {
    TicketStatus.fromString("invalid") should be(None)
  }

  test("toString should return 'reserved' for Reserved status") {
    TicketStatus.Reserved.toString should be("reserved")
  }

  test("toString should return 'purchased' for Purchased status") {
    TicketStatus.Purchased.toString should be("purchased")
  }

  test("toString should return 'cancelled' for Cancelled status") {
    TicketStatus.Cancelled.toString should be("cancelled")
  }
}
