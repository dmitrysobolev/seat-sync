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
  customerAlgebra: CustomerAlgebra[F]
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
        val price = calculatePrice(showtime.price, seat.seatType)
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

      // Save tickets
      savedTickets <- tickets.traverse(ticketAlgebra.create)
      totalPrice: BigDecimal = savedTickets.map(_.price).sum

    } yield ReservationResponse(savedTickets, totalPrice)
  }

  def confirmReservation(ticketIds: List[TicketId]): F[List[Ticket]] = {
    ticketIds.traverse { ticketId =>
      ticketAlgebra.updateStatus(ticketId, TicketStatus.Purchased).flatMap {
        case Some(ticket) => ticket.pure[F]
        case None => MonadThrow[F].raiseError[Ticket](DomainError.TicketNotFound(ticketId))
      }
    }
  }

  def cancelReservation(ticketIds: List[TicketId]): F[List[Ticket]] = {
    ticketIds.traverse { ticketId =>
      ticketAlgebra.updateStatus(ticketId, TicketStatus.Cancelled).flatMap {
        case Some(ticket) => ticket.pure[F]
        case None => MonadThrow[F].raiseError[Ticket](DomainError.TicketNotFound(ticketId))
      }
    }
  }

  def getCustomerTickets(customerId: CustomerId): F[List[Ticket]] = {
    ticketAlgebra.findByCustomer(customerId)
  }

  private def calculatePrice(basePrice: BigDecimal, seatType: SeatType): BigDecimal = {
    seatType match {
      case SeatType.Regular => basePrice
      case SeatType.Premium => basePrice * 1.5
      case SeatType.VIP => basePrice * 2.0
    }
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
    customerAlgebra: CustomerAlgebra[F]
  ): ReservationService[F] = new ReservationService[F](
    movieAlgebra,
    theaterAlgebra,
    showtimeAlgebra,
    seatAlgebra,
    ticketAlgebra,
    customerAlgebra
  )
} 