package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Movie, MovieId}
import com.movietheater.db.DoobieInstances._

class MovieRepository(xa: doobie.Transactor[IO]) {
  def findById(movieId: MovieId): IO[Option[Movie]] = {
    sql"""
      SELECT id, title, description, duration_minutes, rating
      FROM movies
      WHERE id = $movieId
    """.query[Movie].option.transact(xa)
  }

  def findAll: IO[List[Movie]] = {
    sql"""
      SELECT id, title, description, duration_minutes, rating
      FROM movies
    """.query[Movie].stream.compile.toList.transact(xa)
  }

  def create(movie: Movie): IO[Movie] = {
    sql"""
      INSERT INTO movies (id, title, description, duration_minutes, rating)
      VALUES (${movie.id}, ${movie.title}, ${movie.description}, ${movie.durationMinutes}, ${movie.rating})
    """.update.run.transact(xa).map(_ => movie)
  }

  def update(movie: Movie): IO[Movie] = {
    sql"""
      UPDATE movies
      SET title = ${movie.title},
          description = ${movie.description},
          duration_minutes = ${movie.durationMinutes},
          rating = ${movie.rating}
      WHERE id = ${movie.id}
    """.update.run.transact(xa).map(_ => movie)
  }

  def delete(movieId: MovieId): IO[Unit] = {
    sql"""
      DELETE FROM movies
      WHERE id = $movieId
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM movies
    """.update.run.transact(xa).void
  }
} 