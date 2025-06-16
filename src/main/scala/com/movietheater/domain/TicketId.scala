package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.util.UUID

case class TicketId(value: UUID)

object TicketId {
  implicit val show: Show[TicketId] = Show.show(_.value.toString)
} 