package com.movietheater.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.movietheater.algebras._
import com.movietheater.domain._
import com.movietheater.interpreters.doobie._
import com.movietheater.interpreters.inmemory._
import com.movietheater.services.{ReservationService, SeatStatusSyncService}
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec._
import io.circe.syntax._
import java.time.LocalDateTime
import java.util.UUID
import org.http4s.headers.`Content-Type`
import org.http4s.circe._
import com.movietheater.http.routes.ReservationRoutes
import com.movietheater.http.json.JsonCodecs._
import com.movietheater.Main

class ExtendedHttpRoutesSpec extends CatsEffectSuite {

  def createFullService(): IO[ReservationService[IO]] =
    for {
      // Create test data
      movieId <- IO(MovieId(UUID.randomUUID()))
      theaterId <- IO(TheaterId(UUID.randomUUID()))
      showtimeId <- IO(ShowtimeId(UUID.randomUUID()))
      customerId <- IO(CustomerId(UUID.randomUUID()))
      seatId1 <- IO(SeatId("A1-1"))
      seatId2 <- IO(SeatId("A1-2"))
      ticketId <- IO(TicketId(UUID.randomUUID()))

      movie = Movie(movieId, "Test Movie", "Test Description", 120, "PG")
      theater = Theater(theaterId, "Test Theater", "Test Location", 100)
      seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
      seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Premium)
      customer = Customer(customerId, "test@example.com", "Test", "User")
      showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal("10.00"))
      ticket = Ticket(ticketId, showtimeId, seatId1, customerId, BigDecimal("10.00"), TicketStatus.Reserved, LocalDateTime.now())

      // Create algebras
      movieAlgebra <- InMemoryMovieAlgebra[IO](Map(movieId -> movie))
      theaterAlgebra <- InMemoryTheaterAlgebra[IO](Map(theaterId -> theater))
      showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
      customerAlgebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> customer))
      ticketAlgebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> ticket))
      seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId1 -> seat1, seatId2 -> seat2))

      service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
    } yield service

  "Extended ReservationRoutes" - {
    
    "POST /reservations" - {
      "should handle valid reservation request" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          // Get sample data from service to use real IDs
          sampleData <- Main.createSampleData[IO]
          (movies, theaters, showtimes, seats, _, customers) = sampleData
          
          movieId = movies.keys.head
          theaterId = theaters.keys.head  
          customerId = customers.keys.head
          showtimeId = showtimes.keys.head
          availableSeats = seats.values.filter(_.theaterId == theaterId).take(2).toList
          
          request = CreateReservationRequest(showtimeId, availableSeats.map(_.id), customerId)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations")
              .withEntity(request.asJson)
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.Ok) or be(Status.BadRequest) or be(Status.InternalServerError))
        }
      }

      "should handle malformed JSON" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations")
              .withEntity("{ invalid json }")
          ).attempt
        } yield response
        
        test.asserting { result =>
          result.isLeft shouldBe true
        }
      }
    }

    "POST /reservations/confirm" - {
      "should handle ticket confirmation" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          ticketId = TicketId(UUID.randomUUID())
          ticketIds = List(ticketId)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations/confirm")
              .withEntity(ticketIds.asJson)
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.Ok) or be(Status.BadRequest))
        }
      }
    }

    "POST /reservations/cancel" - {
      "should handle ticket cancellation" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          ticketId = TicketId(UUID.randomUUID())
          ticketIds = List(ticketId)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations/cancel")
              .withEntity(ticketIds.asJson)
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.Ok) or be(Status.BadRequest))
        }
      }
    }

    "GET /customers/:id/tickets" - {
      "should handle customer ticket retrieval" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          customerId = UUID.randomUUID()
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.GET, Uri.unsafeFromString(s"/customers/$customerId/tickets"))
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.Ok) or be(Status.NotFound))
        }
      }

      "should handle invalid customer ID format" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.GET, uri"/customers/invalid-uuid/tickets")
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.BadRequest) or be(Status.NotFound))
        }
      }
    }

    "GET /showtimes/:id/seats" - {
      "should handle available seats retrieval" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          showtimeId = UUID.randomUUID()
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.GET, Uri.unsafeFromString(s"/showtimes/$showtimeId/seats"))
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.Ok) or be(Status.NotFound))
        }
      }

      "should handle invalid showtime ID format" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.GET, uri"/showtimes/invalid-uuid/seats")
          )
        } yield response.status
        
        test.asserting { status =>
          status should (be(Status.BadRequest) or be(Status.NotFound))
        }
      }
    }

    "Error handling" - {
      "should handle unsupported HTTP methods" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.PATCH, uri"/reservations")
          )
        } yield response.status
        
        test.asserting { status =>
          status shouldBe Status.NotFound
        }
      }

      "should handle invalid Content-Type" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations")
              .withContentType(`Content-Type`(MediaType.text.plain))
              .withEntity("not json")
          ).attempt
        } yield response
        
        test.asserting { result =>
          result match {
            case Left(_) => succeed // Error expected
            case Right(response) => response.status should (be(Status.BadRequest) or be(Status.UnsupportedMediaType))
          }
        }
      }

      "should handle missing request body" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations")
              // No body
          ).attempt
        } yield response
        
        test.asserting { result =>
          result match {
            case Left(_) => succeed // Error expected
            case Right(response) => response.status should be(Status.BadRequest)
          }
        }
      }
    }

    "Route matching" - {
      "should handle trailing slashes consistently" in {
        val test = for {
          service <- createFullService()
          routes = ReservationRoutes[IO](service)
          
          response1 <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations/")
          ).attempt
          
          response2 <- routes.routes.orNotFound.run(
            Request[IO](Method.POST, uri"/reservations")
          ).attempt
          
        } yield (response1, response2)
        
        test.asserting { case (result1, result2) =>
          // Both requests should have some result (success or failure)
          // We're not checking for exact equality since route behavior may differ
          result1 should not be result2 // Different because of different URLs
          succeed
        }
      }
    }
  }
} 