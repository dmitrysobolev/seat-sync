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
  
  implicit val auditoriumIdEncoder: Encoder[AuditoriumId] = Encoder[UUID].contramap(_.value)
  implicit val auditoriumIdDecoder: Decoder[AuditoriumId] = Decoder[UUID].map(AuditoriumId.apply)
  
  implicit val seatIdEncoder: Encoder[SeatId] = Encoder[String].contramap(_.value)
  implicit val seatIdDecoder: Decoder[SeatId] = Decoder[String].map(SeatId.apply)
  
  // For SeatId as a map key
  implicit val seatIdKeyEncoder: KeyEncoder[SeatId] = KeyEncoder.instance(_.value)
  implicit val seatIdKeyDecoder: KeyDecoder[SeatId] = KeyDecoder.instance(str => Some(SeatId(str)))
  
  // For SeatType as a map key
  implicit val seatTypeKeyEncoder: KeyEncoder[SeatType] = KeyEncoder.instance {
    case SeatType.Standard => "standard"
    case SeatType.Premium  => "premium"
    case SeatType.VIP      => "vip"
  }
  implicit val seatTypeKeyDecoder: KeyDecoder[SeatType] = KeyDecoder.instance {
    case "standard" => Some(SeatType.Standard)
    case "premium"  => Some(SeatType.Premium)
    case "vip"      => Some(SeatType.VIP)
    case _           => None
  }
  
  // For TicketStatus as a map key (if needed in future)
  implicit val ticketStatusKeyEncoder: KeyEncoder[TicketStatus] = KeyEncoder.instance {
    case TicketStatus.Reserved   => "reserved"
    case TicketStatus.Purchased => "purchased"
    case TicketStatus.Cancelled => "cancelled"
  }
  implicit val ticketStatusKeyDecoder: KeyDecoder[TicketStatus] = KeyDecoder.instance {
    case "reserved"   => Some(TicketStatus.Reserved)
    case "purchased"  => Some(TicketStatus.Purchased)
    case "cancelled"  => Some(TicketStatus.Cancelled)
    case _             => None
  }
  
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
  
  // For SeatStatus
  implicit val seatStatusEncoder: Encoder[SeatStatus] = Encoder[String].contramap {
    case SeatStatus.Available => "available"
    case SeatStatus.Reserved  => "reserved"
    case SeatStatus.Sold      => "sold"
  }
  implicit val seatStatusDecoder: Decoder[SeatStatus] = Decoder[String].emap {
    case "available" => Right(SeatStatus.Available)
    case "reserved"  => Right(SeatStatus.Reserved)
    case "sold"      => Right(SeatStatus.Sold)
    case other         => Left(s"Invalid seat status: $other")
  }
  
  // Domain model codecs
  implicit val movieEncoder: Encoder[Movie] = deriveEncoder[Movie]
  implicit val movieDecoder: Decoder[Movie] = deriveDecoder[Movie]
  
  implicit val theaterEncoder: Encoder[Theater] = deriveEncoder[Theater]
  implicit val theaterDecoder: Decoder[Theater] = deriveDecoder[Theater]
  
  implicit val seatEncoder: Encoder[Seat] = deriveEncoder[Seat]
  implicit val seatDecoder: Decoder[Seat] = deriveDecoder[Seat]
  
  // Bring value type codecs into scope for Showtime
  private val _bringValueTypeCodecsIntoScope = (movieIdEncoder, movieIdDecoder, theaterIdEncoder, theaterIdDecoder, showtimeIdEncoder, showtimeIdDecoder, ticketIdEncoder, ticketIdDecoder, customerIdEncoder, customerIdDecoder, seatIdEncoder, seatIdDecoder, moneyEncoder, moneyDecoder)
  
  // Showtime codecs - manual due to complex map types
  implicit val showtimeEncoder: Encoder[Showtime] = Encoder.forProduct8(
    "id", "movieId", "theaterId", "auditoriumId", "startTime", "seatTypes", "seatPrices", "seatStatus"
  )(showtime => (
    showtime.id,
    showtime.movieId,
    showtime.theaterId,
    showtime.auditoriumId,
    showtime.startTime,
    showtime.seatTypes,
    showtime.seatPrices,
    showtime.seatStatus
  ))
  
  implicit val showtimeDecoder: Decoder[Showtime] = Decoder.forProduct8(
    "id", "movieId", "theaterId", "auditoriumId", "startTime", "seatTypes", "seatPrices", "seatStatus"
  )(Showtime.apply)
  
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