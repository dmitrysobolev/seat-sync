package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.LocalDateTime

case class Auditorium(
  id: AuditoriumId,
  theaterId: TheaterId,
  name: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Auditorium {
  implicit val show: Show[Auditorium] = Show.show(auditorium => 
    s"Auditorium ${auditorium.name}"
  )
}