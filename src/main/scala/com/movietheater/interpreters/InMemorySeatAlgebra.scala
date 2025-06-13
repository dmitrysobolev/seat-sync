package com.movietheater.interpreters

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.{SeatAlgebra, TicketAlgebra}

class InMemorySeatAlgebra[F[_]: Sync](
  seatRef: Ref[F, Map[SeatId, Seat]],
  ticketAlgebra: TicketAlgebra[F]
) extends SeatAlgebra[F] {

  def findById(seatId: SeatId): F[Option[Seat]] = {
    seatRef.get.map(_.get(seatId))
  }

  def findByTheater(theaterId: TheaterId): F[List[Seat]] = {
    seatRef.get.map(_.values.filter(_.theaterId == theaterId).toList)
  }

  def findAvailableForShowtime(showtimeId: ShowtimeId): F[List[Seat]] = {
    for {
      allSeats <- seatRef.get.map(_.values.toList)
      reservedTickets <- ticketAlgebra.findByShowtime(showtimeId)
      reservedSeatIds = reservedTickets
        .filter(_.status != TicketStatus.Cancelled)
        .map(_.seatId)
        .toSet
      availableSeats = allSeats.filterNot(seat => reservedSeatIds.contains(seat.id))
    } yield availableSeats
  }

  def create(seat: Seat): F[Seat] = {
    seatRef.modify { seats =>
      val updated = seats + (seat.id -> seat)
      (updated, seat)
    }
  }

  def createMany(seats: List[Seat]): F[List[Seat]] = {
    seatRef.modify { currentSeats =>
      val seatMap = seats.map(s => s.id -> s).toMap
      val updated = currentSeats ++ seatMap
      (updated, seats)
    }
  }

  def update(seat: Seat): F[Option[Seat]] = {
    seatRef.modify { seats =>
      seats.get(seat.id) match {
        case Some(_) =>
          val updated = seats + (seat.id -> seat)
          (updated, Some(seat))
        case None =>
          (seats, None)
      }
    }
  }

  def delete(seatId: SeatId): F[Boolean] = {
    seatRef.modify { seats =>
      seats.get(seatId) match {
        case Some(_) =>
          val updated = seats - seatId
          (updated, true)
        case None =>
          (seats, false)
      }
    }
  }

  def deleteAll(): F[Unit] = seatRef.set(Map.empty)
}

object InMemorySeatAlgebra {
  def apply[F[_]: Sync](
    ticketAlgebra: TicketAlgebra[F],
    initialData: Map[SeatId, Seat] = Map.empty
  ): F[SeatAlgebra[F]] = {
    Ref.of[F, Map[SeatId, Seat]](initialData).map(new InMemorySeatAlgebra[F](_, ticketAlgebra))
  }
} 