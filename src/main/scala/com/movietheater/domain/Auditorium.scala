package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
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
  
  implicit val encoder: Encoder[Auditorium] = Encoder.forProduct5(
    "id", "theaterId", "name", "createdAt", "updatedAt"
  )(auditorium => (
    auditorium.id,
    auditorium.theaterId,
    auditorium.name,
    auditorium.createdAt,
    auditorium.updatedAt
  ))
  
  implicit val decoder: Decoder[Auditorium] = Decoder.forProduct5(
    "id", "theaterId", "name", "createdAt", "updatedAt"
  )(Auditorium.apply)
  
  implicit val get: Get[Auditorium] = Get[(AuditoriumId, TheaterId, String, LocalDateTime, LocalDateTime)].map {
    case (id, theaterId, name, createdAt, updatedAt) =>
      Auditorium(id, theaterId, name, createdAt, updatedAt)
  }
  
  implicit val put: Put[Auditorium] = Put[(AuditoriumId, TheaterId, String, LocalDateTime, LocalDateTime)].contramap { auditorium =>
    (
      auditorium.id,
      auditorium.theaterId,
      auditorium.name,
      auditorium.createdAt,
      auditorium.updatedAt
    )
  }
} 