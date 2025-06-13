package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Showtime, ShowtimeId, MovieId, TheaterId}
import com.movietheater.db.DoobieInstances._
import java.time.LocalDateTime

class ShowtimeRepository(xa: doobie.Transactor[IO]) {
  def findById(showtimeId: ShowtimeId): IO[Option[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, end_time, price
      FROM showtimes
      WHERE id = $showtimeId
    """.query[Showtime].option.transact(xa)
  }

  def findByMovieId(movieId: MovieId): IO[List[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, end_time, price
      FROM showtimes
      WHERE movie_id = $movieId
    """.query[Showtime].stream.compile.toList.transact(xa)
  }

  def findByTheaterId(theaterId: TheaterId): IO[List[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, end_time, price
      FROM showtimes
      WHERE theater_id = $theaterId
    """.query[Showtime].stream.compile.toList.transact(xa)
  }

  def findByTimeRange(from: LocalDateTime, to: LocalDateTime): IO[List[Showtime]] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, end_time, price
      FROM showtimes
      WHERE start_time >= $from AND end_time <= $to
    """.query[Showtime].stream.compile.toList.transact(xa)
  }

  def create(showtime: Showtime): IO[Showtime] = {
    sql"""
      INSERT INTO showtimes (id, movie_id, theater_id, start_time, end_time, price)
      VALUES (${showtime.id}, ${showtime.movieId}, ${showtime.theaterId}, ${showtime.startTime}, ${showtime.endTime}, ${showtime.price})
    """.update.run.transact(xa).map(_ => showtime)
  }

  def update(showtime: Showtime): IO[Showtime] = {
    sql"""
      UPDATE showtimes
      SET movie_id = ${showtime.movieId},
          theater_id = ${showtime.theaterId},
          start_time = ${showtime.startTime},
          end_time = ${showtime.endTime},
          price = ${showtime.price}
      WHERE id = ${showtime.id}
    """.update.run.transact(xa).map(_ => showtime)
  }

  def delete(showtimeId: ShowtimeId): IO[Unit] = {
    sql"""
      DELETE FROM showtimes
      WHERE id = $showtimeId
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM showtimes
    """.update.run.transact(xa).void
  }
} 