package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

case class Showtime(
  id: ShowtimeId,
  movieId: MovieId,
  auditoriumId: AuditoriumId,
  startTime: LocalDateTime,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Showtime {
  implicit val show: Show[Showtime] = Show.show(showtime => 
    s"Showtime at ${showtime.startTime}"
  )
  
  implicit val encoder: Encoder[Showtime] = Encoder.forProduct6(
    "id", "movieId", "auditoriumId", "startTime", "createdAt", "updatedAt"
  )(showtime => (
    showtime.id,
    showtime.movieId,
    showtime.auditoriumId,
    showtime.startTime,
    showtime.createdAt,
    showtime.updatedAt
  ))
  
  implicit val decoder: Decoder[Showtime] = Decoder.forProduct6(
    "id", "movieId", "auditoriumId", "startTime", "createdAt", "updatedAt"
  )(Showtime.apply)
  
  implicit val get: Get[Showtime] = Get[(ShowtimeId, MovieId, AuditoriumId, LocalDateTime, LocalDateTime, LocalDateTime)].map {
    case (id, movieId, auditoriumId, startTime, createdAt, updatedAt) =>
      Showtime(id, movieId, auditoriumId, startTime, createdAt, updatedAt)
  }
  
  implicit val put: Put[Showtime] = Put[(ShowtimeId, MovieId, AuditoriumId, LocalDateTime, LocalDateTime, LocalDateTime)].contramap { showtime =>
    (
      showtime.id,
      showtime.movieId,
      showtime.auditoriumId,
      showtime.startTime,
      showtime.createdAt,
      showtime.updatedAt
    )
  }
} 