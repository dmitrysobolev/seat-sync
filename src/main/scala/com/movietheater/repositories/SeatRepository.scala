package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Seat, SeatId, TheaterId, AuditoriumId, RowNumber, SeatNumber}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}
import java.time.LocalDateTime

class SeatRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Seat] = Get[(SeatId, TheaterId, AuditoriumId, String, Int, LocalDateTime, LocalDateTime)].map {
    case (id, theaterId, auditoriumId, rowNumber, seatNumber, createdAt, updatedAt) =>
      Seat(
        id = id,
        theaterId = theaterId,
        auditoriumId = auditoriumId,
        rowNumber = RowNumber(rowNumber.head),
        seatNumber = SeatNumber(seatNumber),
        createdAt = createdAt,
        updatedAt = updatedAt
      )
  }
  
  implicit val put: Put[Seat] = Put[(SeatId, TheaterId, AuditoriumId, String, Int, LocalDateTime, LocalDateTime)].contramap { seat =>
    (
      seat.id,
      seat.theaterId,
      seat.auditoriumId,
      seat.rowNumber.toString,
      seat.seatNumber.value,
      seat.createdAt,
      seat.updatedAt
    )
  }

  def findById(id: SeatId): IO[Option[Seat]] = {
    sql"""
      SELECT id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at
      FROM seats
      WHERE id = $id
    """.query[Seat].option.transact(xa)
  }

  def findByTheaterId(theaterId: TheaterId): IO[List[Seat]] = {
    sql"""
      SELECT id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at
      FROM seats
      WHERE theater_id = $theaterId
    """.query[Seat].stream.compile.toList.transact(xa)
  }

  def findByAuditoriumId(auditoriumId: AuditoriumId): IO[List[Seat]] = {
    sql"""
      SELECT id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at
      FROM seats
      WHERE auditorium_id = $auditoriumId
    """.query[Seat].stream.compile.toList.transact(xa)
  }

  def create(seat: Seat): IO[Seat] = {
    sql"""
      INSERT INTO seats (id, theater_id, auditorium_id, row_number, seat_number, created_at, updated_at)
      VALUES (${seat.id}, ${seat.theaterId}, ${seat.auditoriumId}, ${seat.rowNumber.toString}, ${seat.seatNumber.value}, ${seat.createdAt}, ${seat.updatedAt})
    """.update.run.transact(xa).map(_ => seat)
  }

  def update(seat: Seat): IO[Seat] = {
    sql"""
      UPDATE seats
      SET theater_id = ${seat.theaterId},
          auditorium_id = ${seat.auditoriumId},
          row_number = ${seat.rowNumber.toString},
          seat_number = ${seat.seatNumber.value},
          updated_at = ${seat.updatedAt}
      WHERE id = ${seat.id}
    """.update.run.transact(xa).map(_ => seat)
  }

  def delete(id: SeatId): IO[Unit] = {
    sql"""
      DELETE FROM seats
      WHERE id = $id
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM seats
    """.update.run.transact(xa).void
  }
} 