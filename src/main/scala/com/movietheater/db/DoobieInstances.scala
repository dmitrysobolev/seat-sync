package com.movietheater.db

import cats.implicits._
import doobie.util.{Get, Put}
import doobie.implicits.javasql.TimestampMeta
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime
import scala.math.BigDecimal

object DoobieInstances {
  // UUID instances
  implicit val uuidGet: Get[UUID] = Get[String].map(UUID.fromString)
  implicit val uuidPut: Put[UUID] = Put[String].contramap(_.toString)

  // Domain ID instances
  implicit val movieIdGet: Get[MovieId] = Get[UUID].map(MovieId.apply)
  implicit val movieIdPut: Put[MovieId] = Put[UUID].contramap(_.value)
  
  implicit val theaterIdGet: Get[TheaterId] = Get[UUID].map(TheaterId.apply)
  implicit val theaterIdPut: Put[TheaterId] = Put[UUID].contramap(_.value)
  
  implicit val showtimeIdGet: Get[ShowtimeId] = Get[UUID].map(ShowtimeId.apply)
  implicit val showtimeIdPut: Put[ShowtimeId] = Put[UUID].contramap(_.value)
  
  implicit val ticketIdGet: Get[TicketId] = Get[UUID].map(TicketId.apply)
  implicit val ticketIdPut: Put[TicketId] = Put[UUID].contramap(_.value)
  
  implicit val customerIdGet: Get[CustomerId] = Get[UUID].map(CustomerId.apply)
  implicit val customerIdPut: Put[CustomerId] = Put[UUID].contramap(_.value)
  
  implicit val seatIdGet: Get[SeatId] = Get[String].map(SeatId.apply)
  implicit val seatIdPut: Put[SeatId] = Put[String].contramap(_.value)

  // LocalDateTime instances
  implicit val localDateTimeGet: Get[LocalDateTime] = Get[java.sql.Timestamp].map(_.toLocalDateTime)
  implicit val localDateTimePut: Put[LocalDateTime] = Put[java.sql.Timestamp].contramap(java.sql.Timestamp.valueOf)

  // BigDecimal instances
  implicit val bigDecimalGet: Get[BigDecimal] = Get[java.math.BigDecimal].map(BigDecimal.apply)
  implicit val bigDecimalPut: Put[BigDecimal] = Put[java.math.BigDecimal].contramap(_.bigDecimal)

  // SeatType instances
  implicit val seatTypeGet: Get[SeatType] = Get[String].map {
    case "Regular" => SeatType.Regular
    case "Premium" => SeatType.Premium
    case "VIP" => SeatType.VIP
    case _ => throw new IllegalArgumentException("Invalid seat type")
  }
  implicit val seatTypePut: Put[SeatType] = Put[String].contramap {
    case SeatType.Regular => "Regular"
    case SeatType.Premium => "Premium"
    case SeatType.VIP => "VIP"
  }

  // TicketStatus instances
  implicit val ticketStatusGet: Get[TicketStatus] = Get[String].map {
    case "Reserved" => TicketStatus.Reserved
    case "Purchased" => TicketStatus.Purchased
    case "Cancelled" => TicketStatus.Cancelled
    case _ => throw new IllegalArgumentException("Invalid ticket status")
  }
  implicit val ticketStatusPut: Put[TicketStatus] = Put[String].contramap {
    case TicketStatus.Reserved => "Reserved"
    case TicketStatus.Purchased => "Purchased"
    case TicketStatus.Cancelled => "Cancelled"
  }
} 