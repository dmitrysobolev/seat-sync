package com.movietheater.services

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.movietheater.algebras._
import com.movietheater.domain._
import com.movietheater.interpreters.doobie._
import com.movietheater.interpreters.inmemory._
import munit.CatsEffectSuite
import java.time.LocalDateTime
import java.util.UUID

class ReservationServiceSpec extends CatsEffectSuite {

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
      val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), Money.fromDollars(10, 0))

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
        response.totalPrice shouldBe Money.fromDollars(25, 0)
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

        val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), Money.fromDollars(10, 0))

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

        val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), Money.fromDollars(10, 0))
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

        val ticket1 = Ticket(ticketId1, showtimeId, seatId1, customerId, Money.fromDollars(10, 0), TicketStatus.Reserved, LocalDateTime.now())
        val ticket2 = Ticket(ticketId2, showtimeId, seatId2, customerId, Money.fromDollars(15, 0), TicketStatus.Reserved, LocalDateTime.now())

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

        val ticket = Ticket(ticketId, showtimeId, seatId, customerId, Money.fromDollars(10, 0), TicketStatus.Reserved, LocalDateTime.now())

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

    "calculatePrice" - {
      "should calculate correct prices for different seat types" in {
        val movieId = MovieId(UUID.randomUUID())
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val basePrice = Money.fromDollars(10, 0)

        val regularSeat = Seat(SeatId("A1-1"), "A1", 1, theaterId, SeatType.Regular)
        val premiumSeat = Seat(SeatId("A1-2"), "A1", 2, theaterId, SeatType.Premium)
        val vipSeat = Seat(SeatId("A1-3"), "A1", 3, theaterId, SeatType.VIP)

        val showtime = Showtime(showtimeId, movieId, theaterId, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), basePrice)
        val customer = Customer(customerId, "test@example.com", "Test", "User")

        val test = for {
          movieAlgebra <- InMemoryMovieAlgebra[IO]()
          theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
          showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
          customerAlgebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> customer))
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(
            regularSeat.id -> regularSeat,
            premiumSeat.id -> premiumSeat,
            vipSeat.id -> vipSeat
          ))

          service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
          request = CreateReservationRequest(showtimeId, List(regularSeat.id, premiumSeat.id, vipSeat.id), customerId)
          response <- service.createReservation(request)
        } yield response

        test.asserting { response =>
          response.tickets should have length 3
          response.totalPrice shouldBe Money.fromDollars(45, 0) // 10.00 + 15.00 + 20.00
          response.tickets.find(_.seatId == regularSeat.id).get.price shouldBe basePrice
          response.tickets.find(_.seatId == premiumSeat.id).get.price shouldBe basePrice * 3 / 2
          response.tickets.find(_.seatId == vipSeat.id).get.price shouldBe basePrice * 2
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

        val ticket1 = Ticket(ticketId1, ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), customerId, Money.fromDollars(10, 0), TicketStatus.Purchased, LocalDateTime.now())
        val ticket2 = Ticket(ticketId2, ShowtimeId(UUID.randomUUID()), SeatId("A1-2"), customerId, Money.fromDollars(15, 0), TicketStatus.Reserved, LocalDateTime.now())
        val ticket3 = Ticket(ticketId3, ShowtimeId(UUID.randomUUID()), SeatId("A1-3"), otherCustomerId, Money.fromDollars(12, 0), TicketStatus.Purchased, LocalDateTime.now())

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

    "Showtime endpoints" - {
      "getAllShowtimes" - {
        "should return all showtimes within the next month" in {
          val movieId = MovieId(UUID.randomUUID())
          val theaterId = TheaterId(UUID.randomUUID())
          val showtimeId1 = ShowtimeId(UUID.randomUUID())
          val showtimeId2 = ShowtimeId(UUID.randomUUID())
          val now = LocalDateTime.now()
          val showtime1 = Showtime(showtimeId1, movieId, theaterId, now.plusHours(1), now.plusHours(3), Money.fromDollars(10, 0))
          val showtime2 = Showtime(showtimeId2, movieId, theaterId, now.plusDays(15), now.plusDays(15).plusHours(2), Money.fromDollars(12, 0))

          val test = for {
            movieAlgebra <- InMemoryMovieAlgebra[IO]()
            theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
            showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId1 -> showtime1, showtimeId2 -> showtime2))
            customerAlgebra <- InMemoryCustomerAlgebra[IO]()
            ticketAlgebra <- InMemoryTicketAlgebra[IO]()
            seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

            service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
            result <- service.getAllShowtimes
          } yield result

          test.asserting { showtimes =>
            showtimes should have length 2
            showtimes should contain theSameElementsAs List(showtime1, showtime2)
          }
        }
      }

      "getShowtimesByMovie" - {
        "should return showtimes for a given movie" in {
          val movieId = MovieId(UUID.randomUUID())
          val theaterId = TheaterId(UUID.randomUUID())
          val showtimeId1 = ShowtimeId(UUID.randomUUID())
          val showtimeId2 = ShowtimeId(UUID.randomUUID())
          val now = LocalDateTime.now()
          val showtime1 = Showtime(showtimeId1, movieId, theaterId, now.plusHours(1), now.plusHours(3), Money.fromDollars(10, 0))
          val showtime2 = Showtime(showtimeId2, movieId, theaterId, now.plusDays(1), now.plusDays(1).plusHours(2), Money.fromDollars(12, 0))

          val test = for {
            movieAlgebra <- InMemoryMovieAlgebra[IO](Map(movieId -> Movie(movieId, "Test Movie", "A test movie", 120, "PG")))
            theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
            showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId1 -> showtime1, showtimeId2 -> showtime2))
            customerAlgebra <- InMemoryCustomerAlgebra[IO]()
            ticketAlgebra <- InMemoryTicketAlgebra[IO]()
            seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

            service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
            result <- service.getShowtimesByMovie(movieId)
          } yield result

          test.asserting { showtimes =>
            showtimes should have length 2
            showtimes should contain theSameElementsAs List(showtime1, showtime2)
          }
        }

        "should fail when movie not found" in {
          val movieId = MovieId(UUID.randomUUID())

          val test = for {
            movieAlgebra <- InMemoryMovieAlgebra[IO]()
            theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
            showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
            customerAlgebra <- InMemoryCustomerAlgebra[IO]()
            ticketAlgebra <- InMemoryTicketAlgebra[IO]()
            seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

            service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
            result <- service.getShowtimesByMovie(movieId).attempt
          } yield result

          test.asserting { result =>
            result.left.toOption.get shouldBe a[DomainError.MovieNotFound]
          }
        }
      }

      "getShowtimesByTheater" - {
        "should return showtimes for a given theater" in {
          val movieId = MovieId(UUID.randomUUID())
          val theaterId = TheaterId(UUID.randomUUID())
          val showtimeId1 = ShowtimeId(UUID.randomUUID())
          val showtimeId2 = ShowtimeId(UUID.randomUUID())
          val now = LocalDateTime.now()
          val showtime1 = Showtime(showtimeId1, movieId, theaterId, now.plusHours(1), now.plusHours(3), Money.fromDollars(10, 0))
          val showtime2 = Showtime(showtimeId2, movieId, theaterId, now.plusDays(1), now.plusDays(1).plusHours(2), Money.fromDollars(12, 0))

          val test = for {
            movieAlgebra <- InMemoryMovieAlgebra[IO]()
            theaterAlgebra <- InMemoryTheaterAlgebra[IO](Map(theaterId -> Theater(theaterId, "Test Theater", "Test Location", 100)))
            showtimeAlgebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId1 -> showtime1, showtimeId2 -> showtime2))
            customerAlgebra <- InMemoryCustomerAlgebra[IO]()
            ticketAlgebra <- InMemoryTicketAlgebra[IO]()
            seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

            service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
            result <- service.getShowtimesByTheater(theaterId)
          } yield result

          test.asserting { showtimes =>
            showtimes should have length 2
            showtimes should contain theSameElementsAs List(showtime1, showtime2)
          }
        }

        "should fail when theater not found" in {
          val theaterId = TheaterId(UUID.randomUUID())

          val test = for {
            movieAlgebra <- InMemoryMovieAlgebra[IO]()
            theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
            showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
            customerAlgebra <- InMemoryCustomerAlgebra[IO]()
            ticketAlgebra <- InMemoryTicketAlgebra[IO]()
            seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)

            service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
            result <- service.getShowtimesByTheater(theaterId).attempt
          } yield result

          test.asserting { result =>
            result.left.toOption.get shouldBe a[DomainError.TheaterNotFound]
          }
        }
      }
    }
  }
} 