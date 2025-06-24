package com.movietheater.json

import com.movietheater.domain.{Ticket, TicketId, ShowtimeId, SeatId, CustomerId, Money, TicketStatus}
import TicketIdCodecs._
import ShowtimeIdCodecs._
import SeatIdCodecs._
import CustomerIdCodecs._
import MoneyCodecs._
import TicketStatusCodecs._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import java.time.LocalDateTime

object TicketCodecs {
  implicit val encoder: Encoder[Ticket] = deriveEncoder[Ticket]
  implicit val decoder: Decoder[Ticket] = deriveDecoder[Ticket]
} 
