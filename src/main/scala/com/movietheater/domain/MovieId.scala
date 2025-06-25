package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.util.UUID

case class MovieId(value: UUID) {
  override def toString: String = value.toString
}

object MovieId {
  implicit val show: Show[MovieId] = Show.show(_.value.toString)
} 