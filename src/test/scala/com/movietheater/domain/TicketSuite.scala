
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.util.UUID
import java.time.LocalDateTime
import cats.implicits._

class TicketSuite extends AnyFunSuite with Matchers {

  test("show instance should return the correct string representation") {
    val ticketId = TicketId(UUID.randomUUID())
    val ticket = Ticket(
      id = ticketId,
      showtimeId = ShowtimeId(UUID.randomUUID()),
      seatId = SeatId("A1"),
      customerId = CustomerId(UUID.randomUUID()),
      price = Money(1000),
      status = TicketStatus.Purchased,
      purchasedAt = LocalDateTime.now()
    )
    ticket.show should be(s"Ticket $ticketId (purchased)")
  }
}
