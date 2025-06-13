package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._

trait SeatAlgebra[F[_]] {
  def findById(seatId: SeatId): F[Option[Seat]]
  def findByTheater(theaterId: TheaterId): F[List[Seat]]
  def findAvailableForShowtime(showtimeId: ShowtimeId): F[List[Seat]]
  def create(seat: Seat): F[Seat]
  def createMany(seats: List[Seat]): F[List[Seat]]
  def update(seat: Seat): F[Option[Seat]]
  def delete(seatId: SeatId): F[Boolean]
  def deleteAll(): F[Unit]
}

object SeatAlgebra {
  def apply[F[_]](implicit ev: SeatAlgebra[F]): SeatAlgebra[F] = ev
} 