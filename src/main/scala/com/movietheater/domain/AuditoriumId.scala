package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.util.UUID

case class AuditoriumId(value: UUID) {
  override def toString: String = value.toString
}

object AuditoriumId {
  implicit val show: Show[AuditoriumId] = Show.show(_.value.toString)
} 