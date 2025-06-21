package com.movietheater.json

import com.movietheater.domain.{Ticket, TicketId, ShowtimeId, SeatId, CustomerId, Money, TicketStatus}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

object TicketCodecs {
  implicit val encoder: Encoder[Ticket] = Encoder.forProduct7(
    "id", "showtimeId", "seatId", "customerId", "price", "status", "purchasedAt"
  )(ticket => (
    ticket.id,
    ticket.showtimeId,
    ticket.seatId,
    ticket.customerId,
    ticket.price,
    ticket.status,
    ticket.purchasedAt
  ))
  
  implicit val decoder: Decoder[Ticket] = Decoder.forProduct7(
    "id", "showtimeId", "seatId", "customerId", "price", "status", "purchasedAt"
  )(Ticket.apply)
} 