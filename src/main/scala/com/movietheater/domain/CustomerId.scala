package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.util.UUID

case class CustomerId(value: UUID)

object CustomerId {
  implicit val show: Show[CustomerId] = Show.show(_.value.toString)
} 