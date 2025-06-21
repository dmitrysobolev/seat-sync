package com.movietheater.json

import com.movietheater.domain.{Movie, MovieId}
import io.circe.{Decoder, Encoder}
import java.time.Duration

object MovieCodecs {
  implicit val encoder: Encoder[Movie] = Encoder.forProduct5(
    "id", "title", "description", "durationMinutes", "rating"
  )(movie => (
    movie.id,
    movie.title,
    movie.description,
    movie.durationMinutes,
    movie.rating
  ))
  
  implicit val decoder: Decoder[Movie] = Decoder.forProduct5(
    "id", "title", "description", "durationMinutes", "rating"
  ) { (id, title, description, durationMinutes, rating) =>
    Movie(
      id = id,
      title = title,
      description = description,
      duration = Duration.ofMinutes(durationMinutes),
      rating = rating
    )
  }
} 