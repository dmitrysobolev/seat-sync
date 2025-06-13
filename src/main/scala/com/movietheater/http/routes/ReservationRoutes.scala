package com.movietheater.http.routes

import cats.effect.{Sync, Concurrent}
import cats.implicits._
import cats.MonadThrow
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import com.movietheater.domain._
import com.movietheater.services.ReservationService
import com.movietheater.http.json.JsonCodecs._
import java.util.UUID
import java.time.LocalDateTime

class ReservationRoutes[F[_]: Concurrent: MonadThrow](reservationService: ReservationService[F]) extends Http4sDsl[F] {
  
  // EntityDecoders and EntityEncoders for our custom types
  implicit val createReservationRequestEntityDecoder: EntityDecoder[F, CreateReservationRequest] = jsonOf[F, CreateReservationRequest]
  implicit val ticketIdListEntityDecoder: EntityDecoder[F, List[TicketId]] = jsonOf[F, List[TicketId]]
  
  implicit val availableSeatsResponseEntityEncoder: EntityEncoder[F, AvailableSeatsResponse] = jsonEncoderOf[F, AvailableSeatsResponse]
  implicit val reservationResponseEntityEncoder: EntityEncoder[F, ReservationResponse] = jsonEncoderOf[F, ReservationResponse]
  implicit val ticketListEntityEncoder: EntityEncoder[F, List[Ticket]] = jsonEncoderOf[F, List[Ticket]]
  implicit val showtimeListEntityEncoder: EntityEncoder[F, List[Showtime]] = jsonEncoderOf[F, List[Showtime]]
  implicit val domainErrorEntityEncoder: EntityEncoder[F, DomainError] = jsonEncoderOf[F, DomainError]
  
  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    // Get all showtimes
    case GET -> Root / "showtimes" =>
      reservationService.getAllShowtimes
        .flatMap(Ok(_))
        .handleErrorWith {
          case error: DomainError => BadRequest(error)
          case _ => InternalServerError("An error occurred")
        }
    
    // Get showtimes by movie
    case GET -> Root / "movies" / CustomUUIDVar(movieId) / "showtimes" =>
      reservationService.getShowtimesByMovie(MovieId(movieId))
        .flatMap(Ok(_))
        .handleErrorWith {
          case error: DomainError => BadRequest(error)
          case _ => InternalServerError("An error occurred")
        }
    
    // Get showtimes by theater
    case GET -> Root / "theaters" / CustomUUIDVar(theaterId) / "showtimes" =>
      reservationService.getShowtimesByTheater(TheaterId(theaterId))
        .flatMap(Ok(_))
        .handleErrorWith {
          case error: DomainError => BadRequest(error)
          case _ => InternalServerError("An error occurred")
        }
    
    // Get available seats for a showtime
    case GET -> Root / "showtimes" / CustomUUIDVar(showtimeId) / "seats" =>
      reservationService.getAvailableSeats(ShowtimeId(showtimeId))
        .flatMap(Ok(_))
        .handleErrorWith {
          case error: DomainError => BadRequest(error)
          case _ => InternalServerError("An error occurred")
        }
    
    // Create a reservation
    case req @ POST -> Root / "reservations" =>
      req.as[CreateReservationRequest].flatMap { request =>
        reservationService.createReservation(request)
          .flatMap(Created(_))
          .handleErrorWith {
            case error: DomainError => BadRequest(error)
            case _ => InternalServerError("An error occurred")
          }
      }
    
    // Confirm (purchase) tickets
    case req @ POST -> Root / "reservations" / "confirm" =>
      req.as[List[TicketId]].flatMap { ticketIds =>
        reservationService.confirmReservation(ticketIds)
          .flatMap(Ok(_))
          .handleErrorWith {
            case error: DomainError => BadRequest(error)
            case _ => InternalServerError("An error occurred")
          }
      }
    
    // Cancel tickets
    case req @ POST -> Root / "reservations" / "cancel" =>
      req.as[List[TicketId]].flatMap { ticketIds =>
        reservationService.cancelReservation(ticketIds)
          .flatMap(Ok(_))
          .handleErrorWith {
            case error: DomainError => BadRequest(error)
            case _ => InternalServerError("An error occurred")
          }
      }
    
    // Get customer tickets
    case GET -> Root / "customers" / CustomUUIDVar(customerId) / "tickets" =>
      reservationService.getCustomerTickets(CustomerId(customerId))
        .flatMap(Ok(_))
        .handleErrorWith {
          case error: DomainError => BadRequest(error)
          case _ => InternalServerError("An error occurred")
        }
  }
  
  object CustomUUIDVar {
    def unapply(str: String): Option[UUID] = {
      scala.util.Try(UUID.fromString(str)).toOption
    }
  }
}

object ReservationRoutes {
  def apply[F[_]: Concurrent: MonadThrow](reservationService: ReservationService[F]): ReservationRoutes[F] =
    new ReservationRoutes[F](reservationService)
} 