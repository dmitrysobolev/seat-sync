package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._
import java.time.LocalDateTime

trait ShowtimeAlgebra[F[_]] {
  def findById(showtimeId: ShowtimeId): F[Option[Showtime]]
  def findByMovie(movieId: MovieId): F[List[Showtime]]
  def findByTheater(theaterId: TheaterId): F[List[Showtime]]
  def findByDateRange(from: LocalDateTime, to: LocalDateTime): F[List[Showtime]]
  def create(showtime: Showtime): F[Showtime]
  def update(showtime: Showtime): F[Option[Showtime]]
  def delete(showtimeId: ShowtimeId): F[Boolean]
  def deleteAll(): F[Unit]
}

object ShowtimeAlgebra {
  def apply[F[_]](implicit ev: ShowtimeAlgebra[F]): ShowtimeAlgebra[F] = ev
} 