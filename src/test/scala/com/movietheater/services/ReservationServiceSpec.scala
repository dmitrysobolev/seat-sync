package com.movietheater.services

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import com.movietheater.interpreters._
import java.util.UUID
import java.time.LocalDateTime

class ReservationServiceSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "ReservationService" - {
    "createReservation" - {
      "should create a reservation successfully" in {
      // Setup test data
      val movieId = MovieId(UUID.randomUUID())
      val theaterId = TheaterId(UUID.randomUUID())
      val showtimeId = ShowtimeId(UUID.randomUUID())
      val customerId = CustomerId(UUID.randomUUID())
      val seatId1 = SeatId("A1-1")
      val seatId2 = SeatId("A1-2")

      val movie = Movie(movieId, "Test Movie", "A test movie", 120, "PG")
      val theater = Theater(theaterId, "Test Theater", "Test Location", 100)
      val seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
      val seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Premium)
      val customer = Customer(customerId, "test@example.com", "Test", "User")
      val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal("10.00"))

      val test = for {
        // Create algebras
        movieAlgebra <- InMemoryMovieAlgebra[IO](Map(movieId -> movie))
        theaterAlgebra <- InMemoryTheaterAlgebra[IO](Map(theaterId -> theater))
        showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
        customerAlgebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> customer))
        ticketAlgebra <- InMemoryTicketAlgebra[IO]()
        seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId1 -> seat1, seatId2 -> seat2))

        // Create service
        service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)

        // Test reservation
        request = CreateReservationRequest(showtimeId, List(seatId1, seatId2), customerId)
        response <- service.createReservation(request)

      } yield response

      test.asserting { response =>
        response.tickets should have length 2
        response.totalPrice shouldBe BigDecimal("25.00")
      }
    }

      "should fail when showtime not found" in {
        val customerId = CustomerId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val seatIds = List(SeatId("A1-1"))

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          request = CreateReservationRequest(showtimeId, seatIds, customerId)
          result <- service.createReservation(request).attempt
        } yield result

        test.asserting { result =>
          result.left.toOption.get shouldBe a[DomainError.ShowtimeNotFound]
        }
      }

      "should fail when customer not found" in {
        val movieId = MovieId(UUID.randomUUID())
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val seatIds = List(SeatId("A1-1"))

        val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal("10.00"))

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          request = CreateReservationRequest(showtimeId, seatIds, customerId)
          result <- service.createReservation(request).attempt
        } yield result

        test.asserting { result =>
          result.left.toOption.get shouldBe a[DomainError.CustomerNotFound]
        }
      }

      "should fail when seats not available" in {
        val movieId = MovieId(UUID.randomUUID())
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val seatId = SeatId("A1-1")

        val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), BigDecimal("10.00"))
        val customer = Customer(customerId, "test@example.com", "Test", "User")

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
          customerAlgebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> customer))
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra) // No seats available

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          request = CreateReservationRequest(showtimeId, List(seatId), customerId)
          result <- service.createReservation(request).attempt
        } yield result

        test.asserting { result =>
          result.left.toOption.get shouldBe a[DomainError.InvalidReservation]
        }
      }
    }

    "confirmReservation" - {
      "should confirm existing tickets" in {
        val ticketId1 = TicketId(UUID.randomUUID())
        val ticketId2 = TicketId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")

        val ticket1 = Ticket(ticketId1, showtimeId, seatId1, customerId, BigDecimal("10.00"), TicketStatus.Reserved, LocalDateTime.now())
        val ticket2 = Ticket(ticketId2, showtimeId, seatId2, customerId, BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO](Map(ticketId1 -> ticket1, ticketId2 -> ticket2))
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          result <- service.confirmReservation(List(ticketId1, ticketId2))
        } yield result

        test.asserting { tickets =>
          tickets should have length 2
          tickets.forall(_.status == TicketStatus.Purchased) shouldBe true
        }
      }

      "should fail when ticket not found" in {
        val ticketId = TicketId(UUID.randomUUID())

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          result <- service.confirmReservation(List(ticketId)).attempt
        } yield result

        test.asserting { result =>
          result.left.toOption.get shouldBe a[DomainError.TicketNotFound]
        }
      }
    }

    "cancelReservation" - {
      "should cancel existing tickets" in {
        val ticketId = TicketId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val seatId = SeatId("A1-1")

        val ticket = Ticket(ticketId, showtimeId, seatId, customerId, BigDecimal("10.00"), TicketStatus.Reserved, LocalDateTime.now())

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> ticket))
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          result <- service.cancelReservation(List(ticketId))
        } yield result

        test.asserting { tickets =>
          tickets should have length 1
          tickets.head.status shouldBe TicketStatus.Cancelled
        }
      }

      "should fail when ticket not found" in {
        val ticketId = TicketId(UUID.randomUUID())

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          result <- service.cancelReservation(List(ticketId)).attempt
        } yield result

        test.asserting { result =>
          result.left.toOption.get shouldBe a[DomainError.TicketNotFound]
        }
      }
    }

    "getCustomerTickets" - {
      "should return customer tickets" in {
        val customerId = CustomerId(UUID.randomUUID())
        val otherCustomerId = CustomerId(UUID.randomUUID())
        val ticketId1 = TicketId(UUID.randomUUID())
        val ticketId2 = TicketId(UUID.randomUUID())
        val ticketId3 = TicketId(UUID.randomUUID())

        val ticket1 = Ticket(ticketId1, ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), customerId, BigDecimal("10.00"), TicketStatus.Purchased, LocalDateTime.now())
        val ticket2 = Ticket(ticketId2, ShowtimeId(UUID.randomUUID()), SeatId("A1-2"), customerId, BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        val ticket3 = Ticket(ticketId3, ShowtimeId(UUID.randomUUID()), SeatId("A1-3"), otherCustomerId, BigDecimal("12.00"), TicketStatus.Purchased, LocalDateTime.now())

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO](Map(ticketId1 -> ticket1, ticketId2 -> ticket2, ticketId3 -> ticket3))
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          result <- service.getCustomerTickets(customerId)
        } yield result

        test.asserting { tickets =>
          tickets should have length 2
          tickets should contain theSameElementsAs List(ticket1, ticket2)
        }
      }

      "should return empty list when customer has no tickets" in {
        val customerId = CustomerId(UUID.randomUUID())

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          result <- service.getCustomerTickets(customerId)
        } yield result

        test.asserting(_ shouldBe List.empty)
      }
    }

    "getAvailableSeats" - {
      "should get available seats for a showtime" in {
        // Setup test data  
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")

        val seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Regular)

        val test = for {
          // Create algebras
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId1 -> seat1, seatId2 -> seat2))

          // Create minimal service (only need seat algebra for this test)
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
          customerAlgebra <- InMemoryCustomerAlgebra[IO]()
          
          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)

          // Test getting available seats
          response <- service.getAvailableSeats(showtimeId)
        } yield response

        test.asserting { response =>
          response.showtimeId shouldBe showtimeId
          response.availableSeats should have length 2
        }
      }
    }
  }
} 