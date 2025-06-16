package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Showtime, ShowtimeId, MovieId, TheaterId, AuditoriumId, SeatId, SeatType, Money, SeatStatus}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}
import java.time.LocalDateTime

class ShowtimeRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Showtime] = Get[(ShowtimeId, MovieId, TheaterId, AuditoriumId, LocalDateTime, LocalDateTime, LocalDateTime)].map {
    case (id, movieId, theaterId, auditoriumId, startTime, createdAt, updatedAt) =>
      Showtime(id, movieId, theaterId, auditoriumId, startTime, Map.empty, Map.empty, Map.empty, createdAt, updatedAt)
  }
  
  implicit val put: Put[Showtime] = Put[(ShowtimeId, MovieId, TheaterId, AuditoriumId, LocalDateTime, LocalDateTime, LocalDateTime)].contramap { showtime =>
    (
      showtime.id,
      showtime.movieId,
      showtime.theaterId,
      showtime.auditoriumId,
      showtime.startTime,
      showtime.createdAt,
      showtime.updatedAt
    )
  }

  def findById(id: ShowtimeId): IO[Option[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at
      FROM showtimes
      WHERE id = $id
    """.query[Showtime].option.transact(xa)
  }

  def findByMovieId(movieId: MovieId): IO[List[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at
      FROM showtimes
      WHERE movie_id = $movieId
    """.query[Showtime].stream.compile.toList.transact(xa)
  }

  def findByTheaterId(theaterId: TheaterId): IO[List[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at
      FROM showtimes
      WHERE theater_id = $theaterId
    """.query[Showtime].stream.compile.toList.transact(xa)
  }

  def findByTimeRange(from: LocalDateTime, to: LocalDateTime): IO[List[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at
      FROM showtimes
      WHERE start_time >= $from AND start_time <= $to
    """.query[Showtime].stream.compile.toList.transact(xa)
  }

  def create(showtime: Showtime): IO[Showtime] = {
    sql"""
      INSERT INTO showtimes (id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at)
      VALUES (${showtime.id}, ${showtime.movieId}, ${showtime.theaterId}, ${showtime.auditoriumId}, ${showtime.startTime}, ${showtime.createdAt}, ${showtime.updatedAt})
    """.update.run.transact(xa).map(_ => showtime)
  }

  def update(showtime: Showtime): IO[Showtime] = {
    sql"""
      UPDATE showtimes
      SET movie_id = ${showtime.movieId},
          theater_id = ${showtime.theaterId},
          auditorium_id = ${showtime.auditoriumId},
          start_time = ${showtime.startTime},
          updated_at = ${showtime.updatedAt}
      WHERE id = ${showtime.id}
    """.update.run.transact(xa).map(_ => showtime)
  }

  def delete(id: ShowtimeId): IO[Unit] = {
    sql"""
      DELETE FROM showtimes
      WHERE id = $id
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM showtimes
    """.update.run.transact(xa).void
  }
} 