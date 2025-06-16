package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

case class Ticket(
  id: TicketId,
  showtimeId: ShowtimeId,
  seatId: SeatId,
  customerId: CustomerId,
  status: TicketStatus,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Ticket {
  implicit val show: Show[Ticket] = Show.show(ticket => 
    s"Ticket ${ticket.id} (${ticket.status})"
  )
  
  implicit val encoder: Encoder[Ticket] = Encoder.forProduct7(
    "id", "showtimeId", "seatId", "customerId", "status", "createdAt", "updatedAt"
  )(ticket => (
    ticket.id,
    ticket.showtimeId,
    ticket.seatId,
    ticket.customerId,
    ticket.status,
    ticket.createdAt,
    ticket.updatedAt
  ))
  
  implicit val decoder: Decoder[Ticket] = Decoder.forProduct7(
    "id", "showtimeId", "seatId", "customerId", "status", "createdAt", "updatedAt"
  )(Ticket.apply)
  
  implicit val get: Get[Ticket] = Get[(TicketId, ShowtimeId, SeatId, CustomerId, String, LocalDateTime, LocalDateTime)].map {
    case (id, showtimeId, seatId, customerId, status, createdAt, updatedAt) =>
      Ticket(
        id = id,
        showtimeId = showtimeId,
        seatId = seatId,
        customerId = customerId,
        status = TicketStatus.fromString(status).getOrElse(TicketStatus.Reserved),
        createdAt = createdAt,
        updatedAt = updatedAt
      )
  }
  
  implicit val put: Put[Ticket] = Put[(TicketId, ShowtimeId, SeatId, CustomerId, String, LocalDateTime, LocalDateTime)].contramap { ticket =>
    (
      ticket.id,
      ticket.showtimeId,
      ticket.seatId,
      ticket.customerId,
      ticket.status.toString,
      ticket.createdAt,
      ticket.updatedAt
    )
  }
} 