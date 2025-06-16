package com.movietheater.interpreters.doobie

import cats.effect.Sync
import cats.implicits._
import com.movietheater.algebras.SeatAlgebra
import com.movietheater.domain.{RowNumber, Seat, SeatNumber, SeatType, ShowtimeId}
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0

class DoobieSeatAlgebra[F[_]: Sync] extends SeatAlgebra[F] {
  import DoobieSeatAlgebra._

  override def getSeatsByShowtime(showtimeId: ShowtimeId): F[List[Seat]] =
    selectSeatsByShowtime(showtimeId).to[List].transact(xa)

  override def getSeatByShowtimeAndPosition(showtimeId: ShowtimeId, rowNumber: RowNumber, seatNumber: SeatNumber): F[Option[Seat]] =
    selectSeatByShowtimeAndPosition(showtimeId, rowNumber, seatNumber).option.transact(xa)

  override def createSeat(seat: Seat): F[Seat] =
    insertSeat(seat).run.transact(xa).as(seat)

  override def createSeats(seats: List[Seat]): F[List[Seat]] =
    insertSeats(seats).run.transact(xa).as(seats)

  override def updateSeat(seat: Seat): F[Seat] =
    updateSeatQuery(seat).run.transact(xa).as(seat)

  override def deleteSeat(seat: Seat): F[Unit] =
    deleteSeatQuery(seat).run.transact(xa).void

  override def deleteSeatsByShowtime(showtimeId: ShowtimeId): F[Unit] =
    deleteSeatsByShowtimeQuery(showtimeId).run.transact(xa).void
}

object DoobieSeatAlgebra {
  def apply[F[_]: Sync]: DoobieSeatAlgebra[F] = new DoobieSeatAlgebra[F]

  private val xa = Transactor.fromDriverManager[doobie.ConnectionIO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://localhost:5432/movietheater",
    user = "postgres",
    password = "postgres"
  )

  private def selectSeatsByShowtime(showtimeId: ShowtimeId): Query0[Seat] =
    sql"""
      SELECT id, showtime_id, row_number, seat_number, seat_type
      FROM seats
      WHERE showtime_id = $showtimeId
      ORDER BY row_number, seat_number
    """.query[Seat]

  private def selectSeatByShowtimeAndPosition(showtimeId: ShowtimeId, rowNumber: RowNumber, seatNumber: SeatNumber): Query0[Seat] =
    sql"""
      SELECT id, showtime_id, row_number, seat_number, seat_type
      FROM seats
      WHERE showtime_id = $showtimeId
      AND row_number = $rowNumber
      AND seat_number = $seatNumber
    """.query[Seat]

  private def insertSeat(seat: Seat): Update0 =
    sql"""
      INSERT INTO seats (id, showtime_id, row_number, seat_number, seat_type)
      VALUES (${seat.id}, ${seat.showtimeId}, ${seat.rowNumber}, ${seat.seatNumber}, ${seat.seatType})
    """.update

  private def insertSeats(seats: List[Seat]): Update0 = {
    val sql = """
      INSERT INTO seats (id, showtime_id, row_number, seat_number, seat_type)
      VALUES (?, ?, ?, ?, ?)
    """
    Update[Seat](sql)
  }

  private def updateSeatQuery(seat: Seat): Update0 =
    sql"""
      UPDATE seats
      SET row_number = ${seat.rowNumber},
          seat_number = ${seat.seatNumber},
          seat_type = ${seat.seatType}
      WHERE id = ${seat.id}
    """.update

  private def deleteSeatQuery(seat: Seat): Update0 =
    sql"""
      DELETE FROM seats
      WHERE id = ${seat.id}
    """.update

  private def deleteSeatsByShowtimeQuery(showtimeId: ShowtimeId): Update0 =
    sql"""
      DELETE FROM seats
      WHERE showtime_id = $showtimeId
    """.update
} 