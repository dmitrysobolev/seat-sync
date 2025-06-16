package com.movietheater.db

import cats.effect.IO
import doobie.util.{Get, Put}
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime

object DoobieInstances {
  // UUID instances
  implicit val uuidGet: Get[UUID] = Get[String].map(UUID.fromString)
  implicit val uuidPut: Put[UUID] = Put[String].contramap(_.toString)

  // ID instances
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

  implicit val auditoriumIdGet: Get[AuditoriumId] = Get[UUID].map(AuditoriumId.apply)
  implicit val auditoriumIdPut: Put[AuditoriumId] = Put[UUID].contramap(_.value)

  // Value objects
  implicit val rowNumberGet: Get[RowNumber] = Get[String].map(str => RowNumber(str.head.toUpper))
  implicit val rowNumberPut: Put[RowNumber] = Put[String].contramap(_.toString)

  implicit val seatNumberGet: Get[SeatNumber] = Get[Int].map(SeatNumber.apply)
  implicit val seatNumberPut: Put[SeatNumber] = Put[Int].contramap(_.value)

  // Enum instances
  implicit val seatTypeGet: Get[SeatType] = Get[String].map {
    case "standard" => SeatType.Standard
    case "premium" => SeatType.Premium
    case "vip" => SeatType.VIP
    case invalid => throw new IllegalArgumentException(s"Invalid seat type: $invalid")
  }

  implicit val seatTypePut: Put[SeatType] = Put[String].contramap {
    case SeatType.Standard => "standard"
    case SeatType.Premium => "premium"
    case SeatType.VIP => "vip"
  }

  implicit val seatStatusGet: Get[SeatStatus] = Get[String].map {
    case "available" => SeatStatus.Available
    case "reserved" => SeatStatus.Reserved
    case "sold" => SeatStatus.Sold
    case invalid => throw new IllegalArgumentException(s"Invalid seat status: $invalid")
  }

  implicit val seatStatusPut: Put[SeatStatus] = Put[String].contramap(_.toString)

  implicit val ticketStatusGet: Get[TicketStatus] = Get[String].map {
    case "reserved" => TicketStatus.Reserved
    case "purchased" => TicketStatus.Purchased
    case "cancelled" => TicketStatus.Cancelled
    case invalid => throw new IllegalArgumentException(s"Invalid ticket status: $invalid")
  }

  implicit val ticketStatusPut: Put[TicketStatus] = Put[String].contramap {
    case TicketStatus.Reserved => "reserved"
    case TicketStatus.Purchased => "purchased"
    case TicketStatus.Cancelled => "cancelled"
  }

  // Money instance
  implicit val moneyGet: Get[Money] = Get[Long].map(Money.fromCents)
  implicit val moneyPut: Put[Money] = Put[Long].contramap(_.cents)

  // DateTime instances
  implicit val localDateTimeGet: Get[LocalDateTime] = Get[String].map(LocalDateTime.parse)
  implicit val localDateTimePut: Put[LocalDateTime] = Put[String].contramap(_.toString)
} 