package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

case class Theater(
  id: TheaterId,
  name: String,
  address: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Theater {
  implicit val show: Show[Theater] = Show.show(theater => 
    s"Theater ${theater.name} (${theater.address})"
  )
  
  implicit val encoder: Encoder[Theater] = Encoder.forProduct5(
    "id", "name", "address", "createdAt", "updatedAt"
  )(theater => (
    theater.id,
    theater.name,
    theater.address,
    theater.createdAt,
    theater.updatedAt
  ))
  
  implicit val decoder: Decoder[Theater] = Decoder.forProduct5(
    "id", "name", "address", "createdAt", "updatedAt"
  )(Theater.apply)
  
  implicit val get: Get[Theater] = Get[(TheaterId, String, String, LocalDateTime, LocalDateTime)].map {
    case (id, name, address, createdAt, updatedAt) =>
      Theater(id, name, address, createdAt, updatedAt)
  }
  
  implicit val put: Put[Theater] = Put[(TheaterId, String, String, LocalDateTime, LocalDateTime)].contramap { theater =>
    (
      theater.id,
      theater.name,
      theater.address,
      theater.createdAt,
      theater.updatedAt
    )
  }
} 