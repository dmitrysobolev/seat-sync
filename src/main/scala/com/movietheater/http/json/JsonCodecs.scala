package com.movietheater.http.json

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import com.movietheater.domain._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

object JsonCodecs {
  
  // Value class codecs
  implicit val movieIdEncoder: Encoder[MovieId] = Encoder[UUID].contramap(_.value)
  implicit val movieIdDecoder: Decoder[MovieId] = Decoder[UUID].map(MovieId.apply)
  
  implicit val theaterIdEncoder: Encoder[TheaterId] = Encoder[UUID].contramap(_.value)
  implicit val theaterIdDecoder: Decoder[TheaterId] = Decoder[UUID].map(TheaterId.apply)
  
  implicit val showtimeIdEncoder: Encoder[ShowtimeId] = Encoder[UUID].contramap(_.value)
  implicit val showtimeIdDecoder: Decoder[ShowtimeId] = Decoder[UUID].map(ShowtimeId.apply)
  
  implicit val ticketIdEncoder: Encoder[TicketId] = Encoder[UUID].contramap(_.value)
  implicit val ticketIdDecoder: Decoder[TicketId] = Decoder[UUID].map(TicketId.apply)
  
  implicit val customerIdEncoder: Encoder[CustomerId] = Encoder[UUID].contramap(_.value)
  implicit val customerIdDecoder: Decoder[CustomerId] = Decoder[UUID].map(CustomerId.apply)
  
  implicit val seatIdEncoder: Encoder[SeatId] = Encoder[String].contramap(_.value)
  implicit val seatIdDecoder: Decoder[SeatId] = Decoder[String].map(SeatId.apply)
  
  // Money codecs
  implicit val moneyEncoder: Encoder[Money] = Encoder[Long].contramap(_.cents)
  implicit val moneyDecoder: Decoder[Money] = Decoder[Long].map(Money.fromCents)
  
  // LocalDateTime codec
  private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = 
    Encoder[String].contramap(_.format(dateTimeFormatter))
  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = 
    Decoder[String].emap(str => 
      scala.util.Try(LocalDateTime.parse(str, dateTimeFormatter))
        .toEither
        .left.map(_ => "Invalid date format")
    )
  
  // Enum codecs
  implicit val seatTypeEncoder: Encoder[SeatType] = Encoder[String].contramap {
    case SeatType.Standard => "standard"
    case SeatType.Premium => "premium"
    case SeatType.VIP => "vip"
  }
  
  implicit val seatTypeDecoder: Decoder[SeatType] = Decoder[String].emap {
    case "standard" => Right(SeatType.Standard)
    case "premium" => Right(SeatType.Premium)
    case "vip" => Right(SeatType.VIP)
    case other => Left(s"Invalid seat type: $other")
  }
  
  implicit val ticketStatusEncoder: Encoder[TicketStatus] = Encoder[String].contramap {
    case TicketStatus.Reserved => "reserved"
    case TicketStatus.Purchased => "purchased"
    case TicketStatus.Cancelled => "cancelled"
  }
  
  implicit val ticketStatusDecoder: Decoder[TicketStatus] = Decoder[String].emap {
    case "reserved" => Right(TicketStatus.Reserved)
    case "purchased" => Right(TicketStatus.Purchased)
    case "cancelled" => Right(TicketStatus.Cancelled)
    case other => Left(s"Invalid ticket status: $other")
  }
  
  // Domain model codecs
  implicit val movieEncoder: Encoder[Movie] = deriveEncoder[Movie]
  implicit val movieDecoder: Decoder[Movie] = deriveDecoder[Movie]
  
  implicit val theaterEncoder: Encoder[Theater] = deriveEncoder[Theater]
  implicit val theaterDecoder: Decoder[Theater] = deriveDecoder[Theater]
  
  implicit val seatEncoder: Encoder[Seat] = deriveEncoder[Seat]
  implicit val seatDecoder: Decoder[Seat] = deriveDecoder[Seat]
  
  implicit val showtimeEncoder: Encoder[Showtime] = deriveEncoder[Showtime]
  implicit val showtimeDecoder: Decoder[Showtime] = deriveDecoder[Showtime]
  
  implicit val ticketEncoder: Encoder[Ticket] = deriveEncoder[Ticket]
  implicit val ticketDecoder: Decoder[Ticket] = deriveDecoder[Ticket]
  
  implicit val customerEncoder: Encoder[Customer] = deriveEncoder[Customer]
  implicit val customerDecoder: Decoder[Customer] = deriveDecoder[Customer]
  
  // Request/Response codecs
  implicit val createReservationRequestEncoder: Encoder[CreateReservationRequest] = deriveEncoder[CreateReservationRequest]
  implicit val createReservationRequestDecoder: Decoder[CreateReservationRequest] = deriveDecoder[CreateReservationRequest]
  
  implicit val reservationResponseEncoder: Encoder[ReservationResponse] = deriveEncoder[ReservationResponse]
  implicit val reservationResponseDecoder: Decoder[ReservationResponse] = deriveDecoder[ReservationResponse]
  
  implicit val availableSeatsResponseEncoder: Encoder[AvailableSeatsResponse] = deriveEncoder[AvailableSeatsResponse]
  implicit val availableSeatsResponseDecoder: Decoder[AvailableSeatsResponse] = deriveDecoder[AvailableSeatsResponse]
  
  // Error codecs
  implicit val domainErrorEncoder: Encoder[DomainError] = Encoder.instance {
    case error => Json.obj(
      "error" -> error.getClass.getSimpleName.asJson,
      "message" -> error.message.asJson
    )
  }
} 