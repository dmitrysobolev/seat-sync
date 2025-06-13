package com.movietheater.interpreters

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.ShowtimeAlgebra
import java.util.UUID
import java.time.LocalDateTime

object DoobieShowtimeAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Showtime
  private implicit val showtimeRead: Read[Showtime] = 
    Read[(UUID, UUID, UUID, LocalDateTime, BigDecimal)].map {
      case (id, movieId, theaterId, startTime, price) =>
        // Calculate endTime based on movie duration - for simplicity, we'll use a default duration
        // In a real implementation, you might join with movies table or store endTime separately
        val endTime = startTime.plusHours(2) // Default 2-hour duration
        Showtime(ShowtimeId(id), MovieId(movieId), TheaterId(theaterId), startTime, endTime, price)
    }
  
  // SQL queries
  private def selectByIdQuery(showtimeId: ShowtimeId): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, price 
      FROM showtimes 
      WHERE id = ${showtimeId.value}
    """.query[Showtime]
  }

  private def selectByMovieQuery(movieId: MovieId): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, price 
      FROM showtimes 
      WHERE movie_id = ${movieId.value}
      ORDER BY start_time
    """.query[Showtime]
  }

  private def selectByTheaterQuery(theaterId: TheaterId): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, price 
      FROM showtimes 
      WHERE theater_id = ${theaterId.value}
      ORDER BY start_time
    """.query[Showtime]
  }

  private def selectByDateRangeQuery(from: LocalDateTime, to: LocalDateTime): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, price 
      FROM showtimes 
      WHERE start_time >= $from AND start_time <= $to
      ORDER BY start_time
    """.query[Showtime]
  }

  private val selectAllQuery: Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, start_time, price 
      FROM showtimes 
      ORDER BY start_time
    """.query[Showtime]
  }

  private def insertQuery(showtime: Showtime): Update0 = {
    sql"""
      INSERT INTO showtimes (id, movie_id, theater_id, start_time, price) 
      VALUES (${showtime.id.value}, ${showtime.movieId.value}, ${showtime.theaterId.value}, ${showtime.startTime}, ${showtime.price})
    """.update
  }

  private def updateQuery(showtime: Showtime): Update0 = {
    sql"""
      UPDATE showtimes 
      SET movie_id = ${showtime.movieId.value}, 
          theater_id = ${showtime.theaterId.value}, 
          start_time = ${showtime.startTime}, 
          price = ${showtime.price},
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${showtime.id.value}
    """.update
  }

  private def deleteQuery(showtimeId: ShowtimeId): Update0 = {
    sql"DELETE FROM showtimes WHERE id = ${showtimeId.value}".update
  }

  private def deleteAllQuery: Update0 = {
    sql"DELETE FROM showtimes".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): ShowtimeAlgebra[F] = 
    new DoobieShowtimeAlgebra[F](xa)
}

class DoobieShowtimeAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends ShowtimeAlgebra[F] {

  import DoobieShowtimeAlgebra._
  import DoobieInstances._

  def findById(showtimeId: ShowtimeId): F[Option[Showtime]] = {
    selectByIdQuery(showtimeId).option.transact(xa)
  }

  def findByMovie(movieId: MovieId): F[List[Showtime]] = {
    selectByMovieQuery(movieId).to[List].transact(xa)
  }

  def findByTheater(theaterId: TheaterId): F[List[Showtime]] = {
    selectByTheaterQuery(theaterId).to[List].transact(xa)
  }

  def findByDateRange(from: LocalDateTime, to: LocalDateTime): F[List[Showtime]] = {
    selectByDateRangeQuery(from, to).to[List].transact(xa)
  }

  def findAll(): F[List[Showtime]] = {
    selectAllQuery.to[List].transact(xa)
  }

  def create(showtime: Showtime): F[Showtime] = {
    insertQuery(showtime).run.transact(xa).as(showtime)
  }

  def update(showtime: Showtime): F[Option[Showtime]] = {
    updateQuery(showtime).run.transact(xa).map {
      case 0 => None
      case _ => Some(showtime)
    }
  }

  def delete(showtimeId: ShowtimeId): F[Boolean] = {
    deleteQuery(showtimeId).run.transact(xa).map(_ > 0)
  }

  def deleteAll(): F[Unit] = {
    deleteAllQuery.run.transact(xa).void
  }
} 