package com.movietheater.interpreters.doobie

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.ShowtimeAlgebra
import java.util.UUID
import java.time.LocalDateTime
import doobie.util.Get
import doobie.util.Read

class DoobieShowtimeAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends ShowtimeAlgebra[F] {
  import DoobieShowtimeAlgebra._

  def findById(showtimeId: ShowtimeId): F[Option[Showtime]] = {
    for {
      showtimeOpt <- selectByIdQuery(showtimeId).option.transact(xa)
      seatTypes <- selectSeatTypesQuery(showtimeId).to[List].transact(xa)
      seatStatuses <- selectSeatStatusesQuery(showtimeId).to[List].transact(xa)
      prices <- selectPricesQuery(showtimeId).to[List].transact(xa)
    } yield showtimeOpt.map(_.copy(
      seatTypes = seatTypes.toMap,
      seatStatuses = seatStatuses.toMap,
      prices = prices.toMap
    ))
  }

  def findByMovie(movieId: MovieId): F[List[Showtime]] = {
    for {
      showtimes <- selectByMovieQuery(movieId).to[List].transact(xa)
      showtimesWithDetails <- showtimes.traverse { showtime =>
        for {
          seatTypes <- selectSeatTypesQuery(showtime.id).to[List].transact(xa)
          seatStatuses <- selectSeatStatusesQuery(showtime.id).to[List].transact(xa)
          prices <- selectPricesQuery(showtime.id).to[List].transact(xa)
        } yield showtime.copy(
          seatTypes = seatTypes.toMap,
          seatStatuses = seatStatuses.toMap,
          prices = prices.toMap
        )
      }
    } yield showtimesWithDetails
  }

  def findByTheater(theaterId: TheaterId): F[List[Showtime]] = {
    for {
      showtimes <- selectByTheaterQuery(theaterId).to[List].transact(xa)
      showtimesWithDetails <- showtimes.traverse { showtime =>
        for {
          seatTypes <- selectSeatTypesQuery(showtime.id).to[List].transact(xa)
          seatStatuses <- selectSeatStatusesQuery(showtime.id).to[List].transact(xa)
          prices <- selectPricesQuery(showtime.id).to[List].transact(xa)
        } yield showtime.copy(
          seatTypes = seatTypes.toMap,
          seatStatuses = seatStatuses.toMap,
          prices = prices.toMap
        )
      }
    } yield showtimesWithDetails
  }

  def findByDateRange(from: LocalDateTime, to: LocalDateTime): F[List[Showtime]] = {
    for {
      showtimes <- selectByDateRangeQuery(from, to).to[List].transact(xa)
      showtimesWithDetails <- showtimes.traverse { showtime =>
        for {
          seatTypes <- selectSeatTypesQuery(showtime.id).to[List].transact(xa)
          seatStatuses <- selectSeatStatusesQuery(showtime.id).to[List].transact(xa)
          prices <- selectPricesQuery(showtime.id).to[List].transact(xa)
        } yield showtime.copy(
          seatTypes = seatTypes.toMap,
          seatStatuses = seatStatuses.toMap,
          prices = prices.toMap
        )
      }
    } yield showtimesWithDetails
  }

  def create(showtime: Showtime): F[Showtime] = {
    for {
      _ <- insertQuery(showtime).run.transact(xa)
      _ <- insertSeatTypesQuery(showtime.id, showtime.seatTypes).run.transact(xa)
      _ <- insertSeatStatusesQuery(showtime.id, showtime.seatStatuses).run.transact(xa)
      _ <- insertPricesQuery(showtime.id, showtime.prices).run.transact(xa)
    } yield showtime
  }

  def update(showtime: Showtime): F[Option[Showtime]] = {
    for {
      updated <- updateQuery(showtime).run.transact(xa)
      _ <- if (updated > 0) {
        for {
          _ <- deleteSeatTypesQuery(showtime.id).run.transact(xa)
          _ <- deleteSeatStatusesQuery(showtime.id).run.transact(xa)
          _ <- deletePricesQuery(showtime.id).run.transact(xa)
          _ <- insertSeatTypesQuery(showtime.id, showtime.seatTypes).run.transact(xa)
          _ <- insertSeatStatusesQuery(showtime.id, showtime.seatStatuses).run.transact(xa)
          _ <- insertPricesQuery(showtime.id, showtime.prices).run.transact(xa)
        } yield ()
      } else ().pure[F]
    } yield if (updated > 0) Some(showtime) else None
  }

  def delete(showtimeId: ShowtimeId): F[Boolean] = {
    for {
      _ <- deleteSeatTypesQuery(showtimeId).run.transact(xa)
      _ <- deleteSeatStatusesQuery(showtimeId).run.transact(xa)
      _ <- deletePricesQuery(showtimeId).run.transact(xa)
      deleted <- deleteQuery(showtimeId).run.transact(xa)
    } yield deleted > 0
  }

  def deleteAll(): F[Unit] = {
    for {
      _ <- sql"DELETE FROM seat_type_assignments".update.run.transact(xa)
      _ <- sql"DELETE FROM seat_statuses".update.run.transact(xa)
      _ <- sql"DELETE FROM seat_type_prices".update.run.transact(xa)
      _ <- sql"DELETE FROM showtimes".update.run.transact(xa)
    } yield ()
  }
}

object DoobieShowtimeAlgebra {
  import DoobieInstances._

  // Row mapping for Showtime
  private implicit val showtimeRead: Read[Showtime] =
    Read[(UUID, UUID, UUID, UUID, LocalDateTime, LocalDateTime, LocalDateTime)].map {
      case (id, movieId, theaterId, auditoriumId, startTime, createdAt, updatedAt) =>
        Showtime(
          id = ShowtimeId(id),
          movieId = MovieId(movieId),
          theaterId = TheaterId(theaterId),
          auditoriumId = AuditoriumId(auditoriumId),
          startTime = startTime,
          seatTypes = Map.empty,    // Loaded separately
          seatPrices = Map.empty,   // Loaded separately
          seatStatus = Map.empty,   // Loaded separately
          createdAt = createdAt,
          updatedAt = updatedAt
        )
    }

  private implicit val seatTypeMoneyRead: Read[(SeatType, Money)] =
    Read[(String, Long)].map { case (seatTypeStr, cents) =>
      val seatType = seatTypeStr match {
        case "standard" => SeatType.Standard
        case "premium" => SeatType.Premium
        case "vip" => SeatType.VIP
        case invalid => throw new IllegalArgumentException(s"Invalid seat type: $invalid")
      }
      (seatType, Money.fromCents(cents))
    }

  private implicit val seatTypeAssignmentRead: Read[(SeatId, SeatType)] =
    Read[(String, String)].map { case (seatIdStr, seatTypeStr) =>
      val seatType = seatTypeStr match {
        case "standard" => SeatType.Standard
        case "premium" => SeatType.Premium
        case "vip" => SeatType.VIP
        case invalid => throw new IllegalArgumentException(s"Invalid seat type: $invalid")
      }
      (SeatId(seatIdStr), seatType)
    }

  private implicit val seatStatusAssignmentRead: Read[(SeatId, SeatStatus)] =
    Read[(String, String)].map { case (seatIdStr, statusStr) =>
      val status = statusStr match {
        case "Available" => SeatStatus.Available
        case "Reserved" => SeatStatus.Reserved
        case "Sold" => SeatStatus.Sold
        case invalid => throw new IllegalArgumentException(s"Invalid seat status: $invalid")
      }
      (SeatId(seatIdStr), status)
    }

  // SQL queries
  private def selectByIdQuery(showtimeId: ShowtimeId): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at 
      FROM showtimes 
      WHERE id = ${showtimeId.value}
    """.query[Showtime]
  }

  private def selectSeatTypesQuery(showtimeId: ShowtimeId): Query0[(SeatId, SeatType)] = {
    sql"""
      SELECT seat_id, seat_type 
      FROM seat_type_assignments 
      WHERE showtime_id = ${showtimeId.value}
    """.query[(SeatId, SeatType)]
  }

  private def selectSeatStatusesQuery(showtimeId: ShowtimeId): Query0[(SeatId, SeatStatus)] = {
    sql"""
      SELECT seat_id, status 
      FROM seat_statuses 
      WHERE showtime_id = ${showtimeId.value}
    """.query[(SeatId, SeatStatus)]
  }

  private def selectPricesQuery(showtimeId: ShowtimeId): Query0[(SeatType, Money)] = {
    sql"""
      SELECT seat_type, price_cents 
      FROM seat_type_prices 
      WHERE showtime_id = ${showtimeId.value}
    """.query[(SeatType, Money)]
  }

  private def selectByMovieQuery(movieId: MovieId): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, end_time, created_at, updated_at 
      FROM showtimes 
      WHERE movie_id = ${movieId.value}
    """.query[Showtime]
  }

  private def selectByTheaterQuery(theaterId: TheaterId): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, end_time, created_at, updated_at 
      FROM showtimes 
      WHERE theater_id = ${theaterId.value}
    """.query[Showtime]
  }

  private def selectByDateRangeQuery(from: LocalDateTime, to: LocalDateTime): Query0[Showtime] = {
    sql"""
      SELECT id, movie_id, theater_id, auditorium_id, start_time, end_time, created_at, updated_at 
      FROM showtimes 
      WHERE start_time >= $from AND start_time <= $to
    """.query[Showtime]
  }

  private def insertQuery(showtime: Showtime): Update0 = {
    sql"""
      INSERT INTO showtimes (
        id, movie_id, theater_id, auditorium_id, start_time, created_at, updated_at
      ) VALUES (
        ${showtime.id.value}, 
        ${showtime.movieId.value}, 
        ${showtime.theaterId.value}, 
        ${showtime.auditoriumId.value}, 
        ${showtime.startTime}, 
        ${showtime.createdAt}, 
        ${showtime.updatedAt}
      )
    """.update
  }

  private def insertSeatTypesQuery(showtimeId: ShowtimeId, seatTypes: Map[SeatId, SeatType]): Update0 = {
    val values = seatTypes.map { case (seatId, seatType) =>
      s"('${showtimeId.value}', '${seatId.value}', '${seatType.toString}')"
    }.mkString(", ")
    Update0(s"INSERT INTO seat_type_assignments (showtime_id, seat_id, seat_type) VALUES $values", None)
  }

  private def insertSeatStatusesQuery(showtimeId: ShowtimeId, seatStatuses: Map[SeatId, SeatStatus]): Update0 = {
    val values = seatStatuses.map { case (seatId, status) =>
      s"('${showtimeId.value}', '${seatId.value}', '${status.toString}')"
    }.mkString(", ")
    Update0(s"INSERT INTO seat_statuses (showtime_id, seat_id, status) VALUES $values", None)
  }

  private def insertPricesQuery(showtimeId: ShowtimeId, prices: Map[SeatType, Money]): Update0 = {
    val values = prices.map { case (seatType, money) =>
      s"('${showtimeId.value}', '${seatType.toString}', ${money.cents})"
    }.mkString(", ")
    Update0(s"INSERT INTO seat_type_prices (showtime_id, seat_type, price_cents) VALUES $values", None)
  }

  private def updateQuery(showtime: Showtime): Update0 = {
    sql"""
      UPDATE showtimes 
      SET movie_id = ${showtime.movieId.value}, 
          theater_id = ${showtime.theaterId.value}, 
          auditorium_id = ${showtime.auditoriumId.value}, 
          start_time = ${showtime.startTime},
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${showtime.id.value}
    """.update
  }

  private def deleteSeatTypesQuery(showtimeId: ShowtimeId): Update0 = {
    sql"DELETE FROM seat_type_assignments WHERE showtime_id = ${showtimeId.value}".update
  }

  private def deleteSeatStatusesQuery(showtimeId: ShowtimeId): Update0 = {
    sql"DELETE FROM seat_statuses WHERE showtime_id = ${showtimeId.value}".update
  }

  private def deletePricesQuery(showtimeId: ShowtimeId): Update0 = {
    sql"DELETE FROM seat_type_prices WHERE showtime_id = ${showtimeId.value}".update
  }

  private def deleteQuery(showtimeId: ShowtimeId): Update0 = {
    sql"DELETE FROM showtimes WHERE id = ${showtimeId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): ShowtimeAlgebra[F] = 
    new DoobieShowtimeAlgebra[F](xa)
} 