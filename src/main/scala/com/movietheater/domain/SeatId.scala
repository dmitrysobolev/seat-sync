package com.movietheater.domain

import cats.Show
import cats.implicits._

case class SeatId(value: String)

object SeatId {
  implicit val show: Show[SeatId] = Show.show(_.value)
}