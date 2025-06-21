package com.movietheater.interpreters.doobie

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.TicketAlgebra
import java.util.UUID
import java.time.LocalDateTime

class DoobieTicketAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends TicketAlgebra[F] {

  import DoobieTicketAlgebra._
  import DoobieInstances._

  def findById(ticketId: TicketId): F[Option[Ticket]] = {
    selectByIdQuery(ticketId).option.transact(xa)
  }

  def findByCustomer(customerId: CustomerId): F[List[Ticket]] = {
    selectByCustomerQuery(customerId).to[List].transact(xa)
  }

  def findByShowtime(showtimeId: ShowtimeId): F[List[Ticket]] = {
    selectByShowtimeQuery(showtimeId).to[List].transact(xa)
  }

  def findBySeatAndShowtime(seatId: SeatId, showtimeId: ShowtimeId): F[Option[Ticket]] = {
    selectBySeatAndShowtimeQuery(seatId, showtimeId).option.transact(xa)
  }

  def create(ticket: Ticket): F[Ticket] = {
    insertQuery(ticket).run.transact(xa).as(ticket)
  }

  def createMany(tickets: List[Ticket]): F[List[Ticket]] = {
    tickets.traverse(create)
  }

  def updateStatus(ticketId: TicketId, status: TicketStatus): F[Option[Ticket]] = {
    for {
      updated <- updateStatusQuery(ticketId, status).run.transact(xa)
      result <- if (updated > 0) findById(ticketId) else MonadCancelThrow[F].pure(None)
    } yield result
  }

  def delete(ticketId: TicketId): F[Boolean] = {
    deleteQuery(ticketId).run.transact(xa).map(_ > 0)
  }
}

object DoobieTicketAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Ticket
  private implicit val ticketRead: Read[Ticket] = 
    Read[(UUID, UUID, String, UUID, Long, TicketStatus, LocalDateTime)].map {
      case (id, showtimeId, seatId, customerId, price, status, reservationTime) =>
        Ticket(
          TicketId(id), 
          ShowtimeId(showtimeId), 
          SeatId(seatId), 
          CustomerId(customerId), 
          Money.fromCents(price), 
          status, 
          reservationTime
        )
    }
  
  // SQL queries
  private def selectByIdQuery(ticketId: TicketId): Query0[Ticket] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, reservation_time
      FROM tickets 
      WHERE id = ${ticketId.value}
    """.query[Ticket]
  }

  private def selectByCustomerQuery(customerId: CustomerId): Query0[Ticket] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, reservation_time
      FROM tickets 
      WHERE customer_id = ${customerId.value}
      ORDER BY reservation_time DESC
    """.query[Ticket]
  }

  private def selectByShowtimeQuery(showtimeId: ShowtimeId): Query0[Ticket] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, reservation_time
      FROM tickets 
      WHERE showtime_id = ${showtimeId.value}
    """.query[Ticket]
  }

  private def selectBySeatAndShowtimeQuery(seatId: SeatId, showtimeId: ShowtimeId): Query0[Ticket] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, reservation_time
      FROM tickets 
      WHERE seat_id = ${seatId.value} AND showtime_id = ${showtimeId.value}
    """.query[Ticket]
  }

  private def insertQuery(ticket: Ticket): Update0 = {
    sql"""
      INSERT INTO tickets (id, showtime_id, seat_id, customer_id, price, status, reservation_time) 
      VALUES (${ticket.id.value}, ${ticket.showtimeId.value}, ${ticket.seatId.value}, ${ticket.customerId.value}, ${ticket.price.cents}, ${ticket.status}, ${ticket.purchasedAt})
    """.update
  }

  private def updateStatusQuery(ticketId: TicketId, status: TicketStatus): Update0 = {
    sql"""
      UPDATE tickets 
      SET status = $status
      WHERE id = ${ticketId.value}
    """.update
  }

  private def deleteQuery(ticketId: TicketId): Update0 = {
    sql"DELETE FROM tickets WHERE id = ${ticketId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): TicketAlgebra[F] = 
    new DoobieTicketAlgebra[F](xa)
} 