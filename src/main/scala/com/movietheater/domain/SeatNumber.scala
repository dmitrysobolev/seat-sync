package com.movietheater.domain

import cats.Show
import cats.implicits._

case class SeatNumber(value: Int) {
  override def toString: String = value.toString
}

object SeatNumber {
  implicit val show: Show[SeatNumber] = Show.show(_.toString)
} 