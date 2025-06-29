package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.util.UUID

case class TheaterId(value: UUID) {
  override def toString: String = value.toString
}

object TheaterId {
  implicit val show: Show[TheaterId] = Show.show(_.value.toString)
} 