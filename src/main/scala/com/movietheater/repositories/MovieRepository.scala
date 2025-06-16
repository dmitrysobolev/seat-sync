package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Movie, MovieId}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}

class MovieRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Movie] = (
    Get[MovieId].map(_.asInstanceOf[MovieId]) <*>
      Get[String] <*>
      Get[String] <*>
      Get[Long] <*>
      Get[String] <*>
      Get[java.time.LocalDateTime] <*>
      Get[java.time.LocalDateTime]
    ).map {
    case (id, title, description, durationMinutes, rating, createdAt, updatedAt) =>
      Movie(
        id = id,
        title = title,
        description = description,
        duration = java.time.Duration.ofMinutes(durationMinutes),
        rating = rating,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
  }
  
  implicit val put: Put[Movie] = (
    Put[MovieId] <*>
      Put[String] <*>
      Put[String] <*>
      Put[Long] <*>
      Put[String] <*>
      Put[java.time.LocalDateTime] <*>
      Put[java.time.LocalDateTime]
    ).contramap { movie =>
    (
      movie.id,
      movie.title,
      movie.description,
      movie.duration.toMinutes,
      movie.rating,
      movie.createdAt,
      movie.updatedAt
    )
  }

  def findById(movieId: MovieId): IO[Option[Movie]] = {
    sql"""
      SELECT id, title, description, duration_minutes, rating, created_at, updated_at
      FROM movies
      WHERE id = $movieId
    """.query[Movie].option.transact(xa)
  }

  def findAll: IO[List[Movie]] = {
    sql"""
      SELECT id, title, description, duration_minutes, rating, created_at, updated_at
      FROM movies
    """.query[Movie].stream.compile.toList.transact(xa)
  }

  def create(movie: Movie): IO[Movie] = {
    sql"""
      INSERT INTO movies (id, title, description, duration_minutes, rating, created_at, updated_at)
      VALUES (${movie.id}, ${movie.title}, ${movie.description}, ${movie.durationMinutes}, 
              ${movie.rating}, ${movie.createdAt}, ${movie.updatedAt})
    """.update.run.transact(xa).map(_ => movie)
  }

  def update(movie: Movie): IO[Movie] = {
    sql"""
      UPDATE movies
      SET title = ${movie.title},
          description = ${movie.description},
          duration_minutes = ${movie.durationMinutes},
          rating = ${movie.rating},
          updated_at = ${movie.updatedAt}
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