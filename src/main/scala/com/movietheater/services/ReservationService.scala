package com.movietheater.services

import cats.effect.Sync
import cats.implicits._
import cats.MonadThrow
import com.movietheater.domain._
import com.movietheater.algebras._
import java.time.LocalDateTime
import java.util.UUID

class ReservationService[F[_]: Sync](
  movieAlgebra: MovieAlgebra[F],
  theaterAlgebra: TheaterAlgebra[F],
  showtimeAlgebra: ShowtimeAlgebra[F],
  seatAlgebra: SeatAlgebra[F],
  ticketAlgebra: TicketAlgebra[F],
  customerAlgebra: CustomerAlgebra[F],
  seatStatusSyncService: SeatStatusSyncService[F]
) {

  def getAvailableSeats(showtimeId: ShowtimeId): F[AvailableSeatsResponse] = {
    for {
      seats <- seatAlgebra.findAvailableForShowtime(showtimeId)
    } yield AvailableSeatsResponse(showtimeId, seats)
  }

  def createReservation(request: CreateReservationRequest): F[ReservationResponse] = {
    for {
      // Validate showtime exists
      showtime <- showtimeAlgebra.findById(request.showtimeId)
        .flatMap {
          case Some(st) => st.pure[F]
          case None => MonadThrow[F].raiseError[Showtime](DomainError.ShowtimeNotFound(request.showtimeId))
        }

      // Validate customer exists
      customer <- customerAlgebra.findById(request.customerId)
        .flatMap {
          case Some(c) => c.pure[F]
          case None => MonadThrow[F].raiseError[Customer](DomainError.CustomerNotFound(request.customerId))
        }

      // Validate all seats exist and are available
      availableSeats <- seatAlgebra.findAvailableForShowtime(request.showtimeId)
      requestedSeatIds = request.seatIds.toSet
      availableSeatIds = availableSeats.map(_.id).toSet

      _ <- if (requestedSeatIds.subsetOf(availableSeatIds)) {
        ().pure[F]
      } else {
        val unavailableSeats = requestedSeatIds -- availableSeatIds
        MonadThrow[F].raiseError(
          DomainError.InvalidReservation(s"Seats not available: ${unavailableSeats.mkString(", ")}")
        )
      }

      // Get seat details for pricing
      seats <- request.seatIds.traverse(seatId =>
        seatAlgebra.findById(seatId).flatMap {
          case Some(seat) => seat.pure[F]
          case None => MonadThrow[F].raiseError[Seat](DomainError.SeatNotAvailable(seatId))
        }
      )

      // Create tickets
      now = LocalDateTime.now()
      tickets: List[Ticket] = seats.map { seat =>
        val price = showtime.getPriceForSeat(seat.id)
          .getOrElse(throw DomainError.InvalidReservation(s"No price defined for seat ${seat.id}"))
        Ticket(
          id = TicketId(UUID.randomUUID()),
          showtimeId = request.showtimeId,
          seatId = seat.id,
          customerId = request.customerId,
          price = price,
          status = TicketStatus.Reserved,
          purchasedAt = now
        )
      }

      // Save tickets and sync seat statuses
      savedTickets <- tickets.traverse { ticket =>
        for {
          savedTicket <- ticketAlgebra.create(ticket)
          _ <- seatStatusSyncService.syncSeatStatus(savedTicket)
        } yield savedTicket
      }
      totalPrice = savedTickets.map(_.price).foldLeft(Money.zero)(_ + _)

    } yield ReservationResponse(savedTickets, totalPrice)
  }

  def confirmReservation(ticketIds: List[TicketId]): F[List[Ticket]] = {
    for {
      tickets <- ticketIds.traverse(ticketAlgebra.findById)
      updatedTickets <- ticketIds.traverse { ticketId =>
        for {
          ticket <- ticketAlgebra.updateStatus(ticketId, TicketStatus.Purchased).flatMap {
            case Some(t) => t.pure[F]
            case None => MonadThrow[F].raiseError[Ticket](DomainError.TicketNotFound(ticketId))
          }
          _ <- seatStatusSyncService.syncSeatStatus(ticket)
        } yield ticket
      }
    } yield updatedTickets
  }

  def cancelReservation(ticketIds: List[TicketId]): F[Unit] = {
    for {
      ticketsOpt <- ticketIds.traverse(ticketAlgebra.findById)
      tickets = ticketsOpt.flatten
      _ <- if (tickets.isEmpty) MonadThrow[F].raiseError(DomainError.InvalidReservation("No valid tickets found for cancellation")) else MonadThrow[F].unit
      showtimeId = tickets.head.showtimeId
      _ <- tickets.traverse { ticket =>
        if (ticket.showtimeId != showtimeId) {
          MonadThrow[F].raiseError(DomainError.InvalidReservation(
            s"Ticket ${ticket.id} belongs to a different showtime"
          ))
        } else {
          MonadThrow[F].pure(())
        }
      }
      // Update ticket statuses and sync seat statuses
      _ <- tickets.traverse { ticket =>
        for {
          updatedTicket <- ticketAlgebra.updateStatus(ticket.id, TicketStatus.Cancelled).flatMap {
            case Some(t) => t.pure[F]
            case None => MonadThrow[F].raiseError[Ticket](DomainError.TicketNotFound(ticket.id))
          }
          _ <- seatStatusSyncService.syncSeatStatus(updatedTicket)
        } yield ()
      }
    } yield ()
  }

  def getCustomerTickets(customerId: CustomerId): F[List[Ticket]] = {
    ticketAlgebra.findByCustomer(customerId)
  }

  // Showtime endpoints
  def getAllShowtimes: F[List[Showtime]] = {
    showtimeAlgebra.findByDateRange(
      LocalDateTime.now(),
      LocalDateTime.now().plusMonths(1)
    )
  }

  def getShowtimesByMovie(movieId: MovieId): F[List[Showtime]] = {
    for {
      _ <- movieAlgebra.findById(movieId).flatMap {
        case None => Sync[F].raiseError(DomainError.MovieNotFound(movieId))
        case Some(_) => Sync[F].unit
      }
      showtimes <- showtimeAlgebra.findByMovie(movieId)
    } yield showtimes
  }

  def getShowtimesByTheater(theaterId: TheaterId): F[List[Showtime]] = {
    for {
      _ <- theaterAlgebra.findById(theaterId).flatMap {
        case None => Sync[F].raiseError(DomainError.TheaterNotFound(theaterId))
        case Some(_) => Sync[F].unit
      }
      showtimes <- showtimeAlgebra.findByTheater(theaterId)
    } yield showtimes
  }
}

object ReservationService {
  def apply[F[_]: Sync](
    movieAlgebra: MovieAlgebra[F],
    theaterAlgebra: TheaterAlgebra[F],
    showtimeAlgebra: ShowtimeAlgebra[F],
    seatAlgebra: SeatAlgebra[F],
    ticketAlgebra: TicketAlgebra[F],
    customerAlgebra: CustomerAlgebra[F],
    seatStatusSyncService: SeatStatusSyncService[F]
  ): ReservationService[F] = new ReservationService[F](
    movieAlgebra,
    theaterAlgebra,
    showtimeAlgebra,
    seatAlgebra,
    ticketAlgebra,
    customerAlgebra,
    seatStatusSyncService
  )
} 