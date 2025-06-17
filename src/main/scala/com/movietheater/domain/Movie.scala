package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.Duration

case class Movie(
  id: MovieId,
  title: String,
  description: String,
  duration: Duration,
  rating: String
) {
  def durationMinutes: Long = duration.toMinutes
}

object Movie {
  implicit val show: Show[Movie] = Show.show(movie => 
    s"Movie ${movie.title} (${movie.duration.toMinutes} min)"
  )
} 