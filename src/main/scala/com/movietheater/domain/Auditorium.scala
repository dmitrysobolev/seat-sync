package com.movietheater.domain

import cats.Show
import cats.implicits._

case class Auditorium(
  id: AuditoriumId,
  theaterId: TheaterId,
  name: String
)

object Auditorium {
  implicit val show: Show[Auditorium] = Show.show(auditorium => 
    s"Auditorium ${auditorium.name}"
  )
}