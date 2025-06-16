package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.util.UUID

case class TicketId(value: UUID)

object TicketId {
  implicit val show: Show[TicketId] = Show.show(_.value.toString)
  
  implicit val encoder: Encoder[TicketId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[TicketId] = Decoder[UUID].map(TicketId.apply)
  
  implicit val get: Get[TicketId] = Get[UUID].map(TicketId.apply)
  implicit val put: Put[TicketId] = Put[UUID].contramap(_.value)
} 