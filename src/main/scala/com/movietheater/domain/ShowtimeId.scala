package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.util.UUID

case class ShowtimeId(value: UUID)

object ShowtimeId {
  implicit val show: Show[ShowtimeId] = Show.show(_.value.toString)
} 