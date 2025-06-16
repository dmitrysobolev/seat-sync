package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.time.{LocalDateTime, Duration}

case class Movie(
  id: MovieId,
  title: String,
  description: String,
  duration: Duration,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Movie {
  implicit val show: Show[Movie] = Show.show(movie => 
    s"Movie ${movie.title} (${movie.duration.toMinutes} min)"
  )
  
  implicit val encoder: Encoder[Movie] = Encoder.forProduct6(
    "id", "title", "description", "duration", "createdAt", "updatedAt"
  )(movie => (
    movie.id,
    movie.title,
    movie.description,
    movie.duration.toMinutes,
    movie.createdAt,
    movie.updatedAt
  ))
  
  implicit val decoder: Decoder[Movie] = Decoder.forProduct6(
    "id", "title", "description", "durationMinutes", "createdAt", "updatedAt"
  ) { (id, title, description, durationMinutes, createdAt, updatedAt) =>
    Movie(
      id = id,
      title = title,
      description = description,
      duration = Duration.ofMinutes(durationMinutes),
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
  
  implicit val get: Get[Movie] = Get[(MovieId, String, String, Long, LocalDateTime, LocalDateTime)].map {
    case (id, title, description, durationMinutes, createdAt, updatedAt) =>
      Movie(
        id = id,
        title = title,
        description = description,
        duration = Duration.ofMinutes(durationMinutes),
        createdAt = createdAt,
        updatedAt = updatedAt
      )
  }
  
  implicit val put: Put[Movie] = Put[(MovieId, String, String, Long, LocalDateTime, LocalDateTime)].contramap { movie =>
    (
      movie.id,
      movie.title,
      movie.description,
      movie.duration.toMinutes,
      movie.createdAt,
      movie.updatedAt
    )
  }
} 