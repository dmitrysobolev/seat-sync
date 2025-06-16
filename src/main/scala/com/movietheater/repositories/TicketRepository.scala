package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Ticket, TicketId, ShowtimeId, SeatId, CustomerId, Money, TicketStatus}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}
import java.time.LocalDateTime

class TicketRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Ticket] = (
    Get[TicketId].map(_.asInstanceOf[TicketId]) <*>
      Get[ShowtimeId].map(_.asInstanceOf[ShowtimeId]) <*>
      Get[SeatId].map(_.asInstanceOf[SeatId]) <*>
      Get[CustomerId].map(_.asInstanceOf[CustomerId]) <*>
      Get[Money] <*>
      Get[String] <*>
      Get[LocalDateTime] <*>
      Get[LocalDateTime] <*>
      Get[LocalDateTime]
    ).map {
    case (id, showtimeId, seatId, customerId, price, status, purchasedAt, createdAt, updatedAt) =>
      Ticket(
        id = id,
        showtimeId = showtimeId,
        seatId = seatId,
        customerId = customerId,
        price = price,
        status = TicketStatus.fromString(status).getOrElse(TicketStatus.Reserved),
        purchasedAt = purchasedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
      )
  }

  implicit val put: Put[Ticket] = (
    Put[TicketId] <*>
      Put[ShowtimeId] <*>
      Put[SeatId] <*>
      Put[CustomerId] <*>
      Put[Money] <*>
      Put[String] <*>
      Put[LocalDateTime] <*>
      Put[LocalDateTime] <*>
      Put[LocalDateTime]
    ).contramap { ticket =>
    (
      ticket.id,
      ticket.showtimeId,
      ticket.seatId,
      ticket.customerId,
      ticket.price,
      ticket.status.toString,
      ticket.purchasedAt,
      ticket.createdAt,
      ticket.updatedAt
    )
  }

  def findById(id: TicketId): IO[Option[Ticket]] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, purchased_at, created_at, updated_at
      FROM tickets
      WHERE id = $id
    """.query[Ticket].option.transact(xa)
  }

  def findByShowtimeId(showtimeId: ShowtimeId): IO[List[Ticket]] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, purchased_at, created_at, updated_at
      FROM tickets
      WHERE showtime_id = $showtimeId
    """.query[Ticket].stream.compile.toList.transact(xa)
  }

  def findByCustomerId(customerId: CustomerId): IO[List[Ticket]] = {
    sql"""
      SELECT id, showtime_id, seat_id, customer_id, price, status, purchased_at, created_at, updated_at
      FROM tickets
      WHERE customer_id = $customerId
    """.query[Ticket].stream.compile.toList.transact(xa)
  }

  def create(ticket: Ticket): IO[Ticket] = {
    sql"""
      INSERT INTO tickets (id, showtime_id, seat_id, customer_id, price, status, purchased_at, created_at, updated_at)
      VALUES (${ticket.id}, ${ticket.showtimeId}, ${ticket.seatId}, ${ticket.customerId}, 
              ${ticket.price}, ${ticket.status.toString}, ${ticket.purchasedAt}, 
              ${ticket.createdAt}, ${ticket.updatedAt})
    """.update.run.transact(xa).map(_ => ticket)
  }

  def update(ticket: Ticket): IO[Ticket] = {
    sql"""
      UPDATE tickets
      SET price = ${ticket.price},
          status = ${ticket.status.toString},
          purchased_at = ${ticket.purchasedAt},
          updated_at = ${ticket.updatedAt}
      WHERE id = ${ticket.id}
    """.update.run.transact(xa).map(_ => ticket)
  }

  def delete(id: TicketId): IO[Unit] = {
    sql"""
      DELETE FROM tickets
      WHERE id = $id
    """.update.run.transact(xa).void
  }
} 