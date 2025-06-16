package com.movietheater.json

import com.movietheater.domain.{Movie, MovieId}
import io.circe.{Decoder, Encoder}
import java.time.{LocalDateTime, Duration}

object MovieCodecs {
  implicit val encoder: Encoder[Movie] = Encoder.forProduct7(
    "id", "title", "description", "duration", "rating", "createdAt", "updatedAt"
  )(movie => (
    movie.id,
    movie.title,
    movie.description,
    movie.duration.toMinutes,
    movie.rating,
    movie.createdAt,
    movie.updatedAt
  ))
  
  implicit val decoder: Decoder[Movie] = Decoder.forProduct7(
    "id", "title", "description", "durationMinutes", "rating", "createdAt", "updatedAt"
  ) { (id, title, description, durationMinutes, rating, createdAt, updatedAt) =>
    Movie(
      id = id,
      title = title,
      description = description,
      duration = Duration.ofMinutes(durationMinutes),
      rating = rating,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }
} 