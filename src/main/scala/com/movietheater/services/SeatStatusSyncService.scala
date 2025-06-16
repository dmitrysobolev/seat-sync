package com.movietheater.services

import cats.effect.Sync
import cats.implicits._
import cats.MonadThrow
import com.movietheater.domain._
import com.movietheater.algebras.{ShowtimeAlgebra, TicketAlgebra}
import java.time.LocalDateTime

class SeatStatusSyncService[F[_]: Sync](
  showtimeAlgebra: ShowtimeAlgebra[F],
  ticketAlgebra: TicketAlgebra[F]
) {
  def syncSeatStatus(ticket: Ticket): F[Unit] = {
    for {
      showtimeOpt <- showtimeAlgebra.findById(ticket.showtimeId)
      showtime <- showtimeOpt match {
        case Some(st) => st.pure[F]
        case None => MonadThrow[F].raiseError[Showtime](DomainError.ShowtimeNotFound(ticket.showtimeId))
      }
      
      // Map ticket status to seat status
      newSeatStatus = ticket.status match {
        case TicketStatus.Reserved => SeatStatus.Reserved
        case TicketStatus.Purchased => SeatStatus.Sold
        case TicketStatus.Cancelled => SeatStatus.Available
      }
      
      // Update seat status in showtime
      updatedShowtime = showtime.copy(
        seatStatus = showtime.seatStatus + (ticket.seatId -> newSeatStatus)
      )
      
      // Save updated showtime
      _ <- showtimeAlgebra.update(updatedShowtime).flatMap {
        case Some(_) => ().pure[F]
        case None => MonadThrow[F].raiseError[Unit](DomainError.ShowtimeNotFound(ticket.showtimeId))
      }
    } yield ()
  }

  def syncSeatStatusesForShowtime(showtimeId: ShowtimeId): F[Unit] = {
    for {
      showtimeOpt <- showtimeAlgebra.findById(showtimeId)
      showtime <- showtimeOpt match {
        case Some(st) => st.pure[F]
        case None => MonadThrow[F].raiseError[Showtime](DomainError.ShowtimeNotFound(showtimeId))
      }
      
      // Get all tickets for the showtime
      tickets <- ticketAlgebra.findByShowtime(showtimeId)
      
      // Group tickets by seat ID and get the most recent status for each seat
      seatStatuses = tickets
        .groupBy(_.seatId)
        .map { case (seatId, seatTickets) =>
          // Sort by purchasedAt to get the most recent status
          val mostRecentTicket = seatTickets.maxBy(_.purchasedAt)
          val seatStatus = mostRecentTicket.status match {
            case TicketStatus.Reserved => SeatStatus.Reserved
            case TicketStatus.Purchased => SeatStatus.Sold
            case TicketStatus.Cancelled => SeatStatus.Available
          }
          (seatId, seatStatus)
        }
      
      // For seats without tickets, set them as Available
      allSeatStatuses = showtime.seatTypes.keySet.foldLeft(seatStatuses) { (acc, seatId) =>
        if (!acc.contains(seatId)) {
          acc + (seatId -> SeatStatus.Available)
        } else {
          acc
        }
      }
      
      // Update showtime with new seat statuses
      updatedShowtime = showtime.copy(seatStatus = allSeatStatuses)
      
      // Save updated showtime
      _ <- showtimeAlgebra.update(updatedShowtime).flatMap {
        case Some(_) => ().pure[F]
        case None => MonadThrow[F].raiseError[Unit](DomainError.ShowtimeNotFound(showtimeId))
      }
    } yield ()
  }
}

object SeatStatusSyncService {
  def apply[F[_]: Sync](
    showtimeAlgebra: ShowtimeAlgebra[F],
    ticketAlgebra: TicketAlgebra[F]
  ): SeatStatusSyncService[F] = new SeatStatusSyncService[F](
    showtimeAlgebra,
    ticketAlgebra
  )
} 