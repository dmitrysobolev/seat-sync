package com.movietheater.interpreters.doobie

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime
import java.math.BigDecimal

object DoobieInstances {
  
  // Meta instances for UUID-based value classes
  implicit val movieIdMeta: Meta[MovieId] = Meta[UUID].imap(MovieId.apply)(_.value)
  implicit val theaterIdMeta: Meta[TheaterId] = Meta[UUID].imap(TheaterId.apply)(_.value)
  implicit val showtimeIdMeta: Meta[ShowtimeId] = Meta[UUID].imap(ShowtimeId.apply)(_.value)
  implicit val ticketIdMeta: Meta[TicketId] = Meta[UUID].imap(TicketId.apply)(_.value)
  implicit val customerIdMeta: Meta[CustomerId] = Meta[UUID].imap(CustomerId.apply)(_.value)
  implicit val seatIdMeta: Meta[SeatId] = Meta[String].imap(SeatId.apply)(_.value)
  
  // SeatType enum mapping to PostgreSQL enum
  implicit val seatTypeMeta: Meta[SeatType] = {
    Meta[String].timap[SeatType](
      {
        case "Premium" => SeatType.Premium
        case "VIP" => SeatType.VIP
        case "Standard" => SeatType.Standard
        case other => throw new IllegalArgumentException(s"Unknown seat type: $other")
      }
    )(
      {
        case SeatType.Premium => "Premium"
        case SeatType.VIP => "VIP"
        case SeatType.Standard => "Standard"
      }
    ).asInstanceOf[Meta[SeatType]]
  }
  
  // TicketStatus enum mapping to PostgreSQL enum
  implicit val ticketStatusMeta: Meta[TicketStatus] = {
    Meta[String].imap(
      {
        case "Reserved" => TicketStatus.Reserved
        case "Confirmed" => TicketStatus.Purchased  // Map DB "Confirmed" to domain "Purchased"
        case "Cancelled" => TicketStatus.Cancelled
        case other => throw new IllegalArgumentException(s"Unknown ticket status: $other")
      }
    )(
      {
        case TicketStatus.Reserved => "Reserved"
        case TicketStatus.Purchased => "Confirmed"  // Map domain "Purchased" to DB "Confirmed"
        case TicketStatus.Cancelled => "Cancelled"
      }
    )
  }
  

} 