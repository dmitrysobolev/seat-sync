
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.time.Duration
import java.util.UUID

class MovieSuite extends AnyFunSuite with Matchers {

  test("durationMinutes should return the correct duration in minutes") {
    val movie = Movie(
      id = MovieId(UUID.randomUUID()),
      title = "The Matrix",
      description = "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
      duration = Duration.ofMinutes(136),
      rating = "R"
    )
    movie.durationMinutes should be(136L)
  }
}
