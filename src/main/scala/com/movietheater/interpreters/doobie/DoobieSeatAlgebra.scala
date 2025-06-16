package com.movietheater.interpreters.doobie

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.{SeatAlgebra, TicketAlgebra}
import java.util.UUID
import java.time.LocalDateTime

class DoobieSeatAlgebra[F[_]: MonadCancelThrow](
  xa: Transactor[F],
  ticketAlgebra: TicketAlgebra[F]
) extends SeatAlgebra[F] {

  import DoobieSeatAlgebra._
  import com.movietheater.db.DoobieInstances._

  def findById(seatId: SeatId): F[Option[Seat]] = {
    selectByIdQuery(seatId).option.transact(xa)
  }

  def findByTheater(theaterId: TheaterId): F[List[Seat]] = {
    selectByTheaterQuery(theaterId).to[List].transact(xa)
  }

  def findAvailableForShowtime(showtimeId: ShowtimeId): F[List[Seat]] = {
    for {
      allSeats <- selectByTheaterQuery(TheaterId(UUID.randomUUID())).to[List].transact(xa) // Need to get theater from showtime
      reservedTickets <- ticketAlgebra.findByShowtime(showtimeId)
      reservedSeatIds = reservedTickets
        .filter(_.status != TicketStatus.Cancelled)
        .map(_.seatId)
        .toSet
      availableSeats = allSeats.filterNot(seat => reservedSeatIds.contains(seat.id))
    } yield availableSeats
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

  // These methods are no longer supported since Seat doesn't have showtimeId
  override def getSeatsByShowtime(showtimeId: ShowtimeId): F[List[Seat]] =
    MonadCancelThrow[F].raiseError(new NotImplementedError("getSeatsByShowtime not supported - Seat no longer has showtimeId"))

  override def getSeatByShowtimeAndPosition(showtimeId: ShowtimeId, rowNumber: RowNumber, seatNumber: SeatNumber): F[Option[Seat]] =
    MonadCancelThrow[F].raiseError(new NotImplementedError("getSeatByShowtimeAndPosition not supported - Seat no longer has showtimeId"))

  override def createSeat(seat: Seat): F[Seat] =
    create(seat)

  override def createSeats(seatList: List[Seat]): F[List[Seat]] =
    createMany(seatList)

  override def updateSeat(seat: Seat): F[Seat] =
    update(seat).map(_.getOrElse(seat))

  override def deleteSeat(seat: Seat): F[Unit] =
    delete(seat.id).void

  override def deleteSeatsByShowtime(showtimeId: ShowtimeId): F[Unit] =
    MonadCancelThrow[F].raiseError(new NotImplementedError("deleteSeatsByShowtime not supported - Seat no longer has showtimeId"))
}

object DoobieSeatAlgebra {
  
  import com.movietheater.db.DoobieInstances._
  
  // Row mapping for Seat
  private implicit val seatRead: Read[Seat] = 
    Read[(String, UUID, UUID, Int, Int, LocalDateTime, LocalDateTime)].map {
      case (id, theaterId, auditoriumId, rowNumber, seatNumber, createdAt, updatedAt) =>
        Seat(
          SeatId(id), 
          TheaterId(theaterId), 
          AuditoriumId(auditoriumId), 
          RowNumber(rowNumber), 
          SeatNumber(seatNumber), 
          createdAt,
          updatedAt
        )
    }
  
  // SQL queries
  private def selectByIdQuery(seatId: SeatId): Query0[Seat] = {
    sql"""
      SELECT id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at 
      FROM seats 
      WHERE id = ${seatId.value}
    """.query[Seat]
  }

  private def selectByTheaterQuery(theaterId: TheaterId): Query0[Seat] = {
    sql"""
      SELECT id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at 
      FROM seats 
      WHERE theater_id = ${theaterId.value}
      ORDER BY row_number, seat_number
    """.query[Seat]
  }

  private def insertQuery(seat: Seat): Update0 = {
    sql"""
      INSERT INTO seats (id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at) 
      VALUES (${seat.id.value}, ${seat.theaterId.value}, ${seat.auditoriumId.value}, ${seat.rowNumber.value}, ${seat.seatNumber.value}, ${seat.createdAt}, ${seat.updatedAt})
    """.update
  }

  private def updateQuery(seat: Seat): Update0 = {
    sql"""
      UPDATE seats 
      SET theater_id = ${seat.theaterId.value}, 
          auditorium_id = ${seat.auditoriumId.value}, 
          row_number = ${seat.rowNumber.value},
          seat_number = ${seat.seatNumber.value},
          updated_at = ${seat.updatedAt}
      WHERE id = ${seat.id.value}
    """.update
  }

  private def deleteQuery(seatId: SeatId): Update0 = {
    sql"DELETE FROM seats WHERE id = ${seatId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F], ticketAlgebra: TicketAlgebra[F]): SeatAlgebra[F] = 
    new DoobieSeatAlgebra[F](xa, ticketAlgebra)
}