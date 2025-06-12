package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._

trait TicketAlgebra[F[_]] {
  def findById(ticketId: TicketId): F[Option[Ticket]]
  def findByCustomer(customerId: CustomerId): F[List[Ticket]]
  def findByShowtime(showtimeId: ShowtimeId): F[List[Ticket]]
  def findBySeatAndShowtime(seatId: SeatId, showtimeId: ShowtimeId): F[Option[Ticket]]
  def create(ticket: Ticket): F[Ticket]
  def updateStatus(ticketId: TicketId, status: TicketStatus): F[Option[Ticket]]
  def delete(ticketId: TicketId): F[Boolean]
}

object TicketAlgebra {
  def apply[F[_]](implicit ev: TicketAlgebra[F]): TicketAlgebra[F] = ev
} 