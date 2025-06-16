package com.movietheater.domain

sealed abstract class DomainError(val message: String) extends Exception(message)

object DomainError {
  case class ShowtimeNotFound(showtimeId: ShowtimeId) extends DomainError(s"Showtime not found: ${showtimeId.value}")
  case class CustomerNotFound(customerId: CustomerId) extends DomainError(s"Customer not found: ${customerId.value}")
  case class MovieNotFound(movieId: MovieId) extends DomainError(s"Movie not found: ${movieId.value}")
  case class TheaterNotFound(theaterId: TheaterId) extends DomainError(s"Theater not found: ${theaterId.value}")
  case class TicketNotFound(ticketId: TicketId) extends DomainError(s"Ticket not found: ${ticketId.value}")
  case class SeatNotAvailable(seatId: SeatId) extends DomainError(s"Seat not available: ${seatId.value}")
  case class InvalidReservation(reason: String) extends DomainError(s"Invalid reservation: $reason")
}