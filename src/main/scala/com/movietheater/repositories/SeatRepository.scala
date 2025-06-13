package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Seat, SeatId, TheaterId}
import com.movietheater.db.DoobieInstances._

class SeatRepository(xa: doobie.Transactor[IO]) {
  def findById(seatId: SeatId): IO[Option[Seat]] = {
    sql"""
      SELECT id, row, number, theater_id, seat_type
      FROM seats
      WHERE id = $seatId
    """.query[Seat].option.transact(xa)
  }

  def findByTheaterId(theaterId: TheaterId): IO[List[Seat]] = {
    sql"""
      SELECT id, row, number, theater_id, seat_type
      FROM seats
      WHERE theater_id = $theaterId
    """.query[Seat].stream.compile.toList.transact(xa)
  }

  def create(seat: Seat): IO[Seat] = {
    sql"""
      INSERT INTO seats (id, row, number, theater_id, seat_type)
      VALUES (${seat.id}, ${seat.row}, ${seat.number}, ${seat.theaterId}, ${seat.seatType})
    """.update.run.transact(xa).map(_ => seat)
  }

  def update(seat: Seat): IO[Seat] = {
    sql"""
      UPDATE seats
      SET row = ${seat.row},
          number = ${seat.number},
          theater_id = ${seat.theaterId},
          seat_type = ${seat.seatType}
      WHERE id = ${seat.id}
    """.update.run.transact(xa).map(_ => seat)
  }

  def delete(seatId: SeatId): IO[Unit] = {
    sql"""
      DELETE FROM seats
      WHERE id = $seatId
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM seats
    """.update.run.transact(xa).void
  }
} 