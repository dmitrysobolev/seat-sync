package com.movietheater.domain

import java.time.LocalDateTime
import java.util.UUID

// Core domain models
case class MovieId(value: UUID) extends AnyVal
case class TheaterId(value: UUID) extends AnyVal
case class ShowtimeId(value: UUID) extends AnyVal
case class TicketId(value: UUID) extends AnyVal
case class CustomerId(value: UUID) extends AnyVal
case class SeatId(value: String) extends AnyVal

case class Movie(
  id: MovieId,
  title: String,
  description: String,
  durationMinutes: Int,
  rating: String
)

case class Theater(
  id: TheaterId,
  name: String,
  location: String,
  totalSeats: Int
)

case class Seat(
  id: SeatId,
  row: String,
  number: Int,
  theaterId: TheaterId,
  seatType: SeatType
)

sealed trait SeatType
object SeatType {
  case object Regular extends SeatType
  case object Premium extends SeatType
  case object VIP extends SeatType
}

case class Showtime(
  id: ShowtimeId,
  movieId: MovieId,
  theaterId: TheaterId,
  startTime: LocalDateTime,
  endTime: LocalDateTime,
  price: BigDecimal
)

case class Ticket(
  id: TicketId,
  showtimeId: ShowtimeId,
  seatId: SeatId,
  customerId: CustomerId,
  price: BigDecimal,
  status: TicketStatus,
  purchasedAt: LocalDateTime
)

sealed trait TicketStatus
object TicketStatus {
  case object Reserved extends TicketStatus
  case object Purchased extends TicketStatus
  case object Cancelled extends TicketStatus
}

case class Customer(
  id: CustomerId,
  email: String,
  firstName: String,
  lastName: String
)

// Request/Response models
case class CreateReservationRequest(
  showtimeId: ShowtimeId,
  seatIds: List[SeatId],
  customerId: CustomerId
)

case class ReservationResponse(
  tickets: List[Ticket],
  totalPrice: BigDecimal
)

case class AvailableSeatsResponse(
  showtimeId: ShowtimeId,
  availableSeats: List[Seat]
)

// Errors
sealed trait DomainError extends Throwable {
  def message: String
  override def getMessage: String = message
}

object DomainError {
  case class MovieNotFound(movieId: MovieId) extends DomainError {
    def message: String = s"Movie with id ${movieId.value} not found"
  }
  
  case class TheaterNotFound(theaterId: TheaterId) extends DomainError {
    def message: String = s"Theater with id ${theaterId.value} not found"
  }
  
  case class ShowtimeNotFound(showtimeId: ShowtimeId) extends DomainError {
    def message: String = s"Showtime with id ${showtimeId.value} not found"
  }
  
  case class SeatNotAvailable(seatId: SeatId) extends DomainError {
    def message: String = s"Seat ${seatId.value} is not available"
  }
  
  case class CustomerNotFound(customerId: CustomerId) extends DomainError {
    def message: String = s"Customer with id ${customerId.value} not found"
  }
  
  case class TicketNotFound(ticketId: TicketId) extends DomainError {
    def message: String = s"Ticket with id ${ticketId.value} not found"
  }
  
  case class InvalidReservation(reason: String) extends DomainError {
    def message: String = s"Invalid reservation: $reason"
  }
} 