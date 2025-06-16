package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.LocalDateTime

case class Theater(
  id: TheaterId,
  name: String,
  address: String,
  totalSeats: Int,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Theater {
  implicit val show: Show[Theater] = Show.show(theater => 
    s"Theater ${theater.name} (${theater.address})"
  )
} 