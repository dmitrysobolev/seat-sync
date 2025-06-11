package com.movietheater.interpreters

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.TicketAlgebra

class InMemoryTicketAlgebra[F[_]: Sync](ref: Ref[F, Map[TicketId, Ticket]]) extends TicketAlgebra[F] {

  def findById(ticketId: TicketId): F[Option[Ticket]] = {
    ref.get.map(_.get(ticketId))
  }

  def findByCustomer(customerId: CustomerId): F[List[Ticket]] = {
    ref.get.map(_.values.filter(_.customerId == customerId).toList)
  }

  def findByShowtime(showtimeId: ShowtimeId): F[List[Ticket]] = {
    ref.get.map(_.values.filter(_.showtimeId == showtimeId).toList)
  }

  def create(ticket: Ticket): F[Ticket] = {
    ref.modify { tickets =>
      val updated = tickets + (ticket.id -> ticket)
      (updated, ticket)
    }
  }

  def createMany(tickets: List[Ticket]): F[List[Ticket]] = {
    ref.modify { currentTickets =>
      val ticketMap = tickets.map(t => t.id -> t).toMap
      val updated = currentTickets ++ ticketMap
      (updated, tickets)
    }
  }

  def updateStatus(ticketId: TicketId, status: TicketStatus): F[Option[Ticket]] = {
    ref.modify { tickets =>
      tickets.get(ticketId) match {
        case Some(ticket) =>
          val updatedTicket = ticket.copy(status = status)
          val updated = tickets + (ticketId -> updatedTicket)
          (updated, Some(updatedTicket))
        case None =>
          (tickets, None)
      }
    }
  }

  def delete(ticketId: TicketId): F[Boolean] = {
    ref.modify { tickets =>
      tickets.get(ticketId) match {
        case Some(_) =>
          val updated = tickets - ticketId
          (updated, true)
        case None =>
          (tickets, false)
      }
    }
  }
}

object InMemoryTicketAlgebra {
  def apply[F[_]: Sync](initialData: Map[TicketId, Ticket] = Map.empty): F[TicketAlgebra[F]] = {
    Ref.of[F, Map[TicketId, Ticket]](initialData).map(new InMemoryTicketAlgebra[F](_))
  }
} 