package com.movietheater.domain

import cats.Show
import cats.implicits._

case class Theater(
  id: TheaterId,
  name: String,
  address: String,
  totalSeats: Int
)

object Theater {
  implicit val show: Show[Theater] = Show.show(theater => 
    s"Theater ${theater.name} (${theater.address})"
  )
} 