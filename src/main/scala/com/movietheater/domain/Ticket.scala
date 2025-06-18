package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.LocalDateTime

case class Ticket(
  id: TicketId,
  showtimeId: ShowtimeId,
  seatId: SeatId,
  customerId: CustomerId,
  price: Money,
  status: TicketStatus,
  purchasedAt: LocalDateTime
)

object Ticket {
  implicit val show: Show[Ticket] = Show.show(ticket => 
    s"Ticket ${ticket.id} (${ticket.status})"
  )
} 