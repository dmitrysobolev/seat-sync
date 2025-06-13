package com.movietheater.interpreters

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.{SeatAlgebra, TicketAlgebra}
import java.util.UUID

class DoobieSeatAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F], ticketAlgebra: TicketAlgebra[F]) extends SeatAlgebra[F] {

  import DoobieSeatAlgebra._
  import DoobieInstances._

  def findById(seatId: SeatId): F[Option[Seat]] = {
    selectByIdQuery(seatId).option.transact(xa)
  }

  def findByTheater(theaterId: TheaterId): F[List[Seat]] = {
    selectByTheaterQuery(theaterId).to[List].transact(xa)
  }

  def findAvailable(showtimeId: ShowtimeId): F[List[Seat]] = {
    for {
      tickets <- ticketAlgebra.findByShowtime(showtimeId)
      bookedSeatIds = tickets.map(_.seatId).toSet
      allSeats <- findByShowtimeTheater(showtimeId)
    } yield allSeats.filterNot(seat => bookedSeatIds.contains(seat.id))
  }

  def findAvailableForShowtime(showtimeId: ShowtimeId): F[List[Seat]] = {
    findAvailable(showtimeId)
  }

  def isAvailable(seatId: SeatId, showtimeId: ShowtimeId): F[Boolean] = {
    ticketAlgebra.findBySeatAndShowtime(seatId, showtimeId).map(_.isEmpty)
  }

  def create(seat: Seat): F[Seat] = {
    insertQuery(seat).run.transact(xa).as(seat)
  }

  def createMany(seats: List[Seat]): F[List[Seat]] = {
    seats.traverse(create)
  }

  def update(seat: Seat): F[Option[Seat]] = {
    updateQuery(seat).run.transact(xa).map {
      case 0 => None
      case _ => Some(seat)
    }
  }

  def delete(seatId: SeatId): F[Boolean] = {
    deleteQuery(seatId).run.transact(xa).map(_ > 0)
  }

  def deleteAll(): F[Unit] = {
    sql"DELETE FROM seats".update.run.transact(xa).void
  }

  private def findByShowtimeTheater(showtimeId: ShowtimeId): F[List[Seat]] = {
    selectByShowtimeTheaterQuery(showtimeId).to[List].transact(xa)
  }
}

object DoobieSeatAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Seat
  private implicit val seatRead: Read[Seat] = 
    Read[(String, UUID, String, Int, SeatType)].map {
      case (id, theaterId, row, seatNumber, seatType) =>
        Seat(SeatId(id), row, seatNumber, TheaterId(theaterId), seatType)
    }
  
  // SQL queries
  private def selectByIdQuery(seatId: SeatId): Query0[Seat] = {
    sql"""
      SELECT id, theater_id, row_number, seat_number, seat_type 
      FROM seats 
      WHERE id = ${seatId.value}
    """.query[Seat]
  }

  private def selectByTheaterQuery(theaterId: TheaterId): Query0[Seat] = {
    sql"""
      SELECT id, theater_id, row_number, seat_number, seat_type 
      FROM seats 
      WHERE theater_id = ${theaterId.value} 
      ORDER BY row_number, seat_number
    """.query[Seat]
  }

  private def selectByShowtimeTheaterQuery(showtimeId: ShowtimeId): Query0[Seat] = {
    sql"""
      SELECT s.id, s.theater_id, s.row_number, s.seat_number, s.seat_type 
      FROM seats s
      JOIN showtimes st ON s.theater_id = st.theater_id
      WHERE st.id = ${showtimeId.value}
      ORDER BY s.row_number, s.seat_number
    """.query[Seat]
  }

  private def insertQuery(seat: Seat): Update0 = {
    sql"""
      INSERT INTO seats (id, theater_id, row_number, seat_number, seat_type) 
      VALUES (${seat.id.value}, ${seat.theaterId.value}, ${seat.row}, ${seat.number}, ${seat.seatType}::seat_type)
    """.update
  }

  private def updateQuery(seat: Seat): Update0 = {
    sql"""
      UPDATE seats 
      SET theater_id = ${seat.theaterId.value}, 
          row_number = ${seat.row}, 
          seat_number = ${seat.number}, 
          seat_type = ${seat.seatType}::seat_type,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${seat.id.value}
    """.update
  }

  private def deleteQuery(seatId: SeatId): Update0 = {
    sql"DELETE FROM seats WHERE id = ${seatId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](
    xa: Transactor[F], 
    ticketAlgebra: TicketAlgebra[F]
  ): SeatAlgebra[F] = 
    new DoobieSeatAlgebra[F](xa, ticketAlgebra)
} 