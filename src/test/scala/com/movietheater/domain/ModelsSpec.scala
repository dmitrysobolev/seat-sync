package com.movietheater.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.util.UUID
import java.time.LocalDateTime

class ModelsSpec extends AnyWordSpec with Matchers {

  "MovieId" should {
    "wrap UUID correctly" in {
      val uuid = UUID.randomUUID()
      val movieId = MovieId(uuid)
      movieId.value shouldBe uuid
    }
  }

  "TheaterId" should {
    "wrap UUID correctly" in {
      val uuid = UUID.randomUUID()
      val theaterId = TheaterId(uuid)
      theaterId.value shouldBe uuid
    }
  }

  "ShowtimeId" should {
    "wrap UUID correctly" in {
      val uuid = UUID.randomUUID()
      val showtimeId = ShowtimeId(uuid)
      showtimeId.value shouldBe uuid
    }
  }

  "TicketId" should {
    "wrap UUID correctly" in {
      val uuid = UUID.randomUUID()
      val ticketId = TicketId(uuid)
      ticketId.value shouldBe uuid
    }
  }

  "CustomerId" should {
    "wrap UUID correctly" in {
      val uuid = UUID.randomUUID()
      val customerId = CustomerId(uuid)
      customerId.value shouldBe uuid
    }
  }

  "SeatId" should {
    "wrap String correctly" in {
      val seatId = SeatId("A1-5")
      seatId.value shouldBe "A1-5"
    }
  }

  "SeatType" should {
    "have Regular type" in {
      SeatType.Regular shouldBe SeatType.Regular
    }

    "have Premium type" in {
      SeatType.Premium shouldBe SeatType.Premium
    }

    "have VIP type" in {
      SeatType.VIP shouldBe SeatType.VIP
    }
  }

  "TicketStatus" should {
    "have Reserved status" in {
      TicketStatus.Reserved shouldBe TicketStatus.Reserved
    }

    "have Purchased status" in {
      TicketStatus.Purchased shouldBe TicketStatus.Purchased
    }

    "have Cancelled status" in {
      TicketStatus.Cancelled shouldBe TicketStatus.Cancelled
    }
  }

  "Movie" should {
    "be created with all fields" in {
      val movieId = MovieId(UUID.randomUUID())
      val movie = Movie(movieId, "Test Movie", "A test description", 120, "PG-13")
      
      movie.id shouldBe movieId
      movie.title shouldBe "Test Movie"
      movie.description shouldBe "A test description"
      movie.durationMinutes shouldBe 120
      movie.rating shouldBe "PG-13"
    }
  }

  "Theater" should {
    "be created with all fields" in {
      val theaterId = TheaterId(UUID.randomUUID())
      val theater = Theater(theaterId, "Test Theater", "Test Location", 150)
      
      theater.id shouldBe theaterId
      theater.name shouldBe "Test Theater"
      theater.location shouldBe "Test Location"
      theater.totalSeats shouldBe 150
    }
  }

  "Seat" should {
    "be created with all fields" in {
      val seatId = SeatId("A1-5")
      val theaterId = TheaterId(UUID.randomUUID())
      val seat = Seat(seatId, "A1", 5, theaterId, SeatType.Premium)
      
      seat.id shouldBe seatId
      seat.row shouldBe "A1"
      seat.number shouldBe 5
      seat.theaterId shouldBe theaterId
      seat.seatType shouldBe SeatType.Premium
    }
  }

  "Showtime" should {
    "be created with all fields" in {
      val showtimeId = ShowtimeId(UUID.randomUUID())
      val movieId = MovieId(UUID.randomUUID())
      val theaterId = TheaterId(UUID.randomUUID())
      val startTime = LocalDateTime.now()
      val endTime = startTime.plusHours(2)
      val price = BigDecimal("15.50")
      
      val showtime = Showtime(showtimeId, movieId, theaterId, startTime, endTime, price)
      
      showtime.id shouldBe showtimeId
      showtime.movieId shouldBe movieId
      showtime.theaterId shouldBe theaterId
      showtime.startTime shouldBe startTime
      showtime.endTime shouldBe endTime
      showtime.price shouldBe price
    }
  }

  "Ticket" should {
    "be created with all fields" in {
      val ticketId = TicketId(UUID.randomUUID())
      val showtimeId = ShowtimeId(UUID.randomUUID())
      val seatId = SeatId("A1-5")
      val customerId = CustomerId(UUID.randomUUID())
      val price = BigDecimal("20.00")
      val purchasedAt = LocalDateTime.now()
      
      val ticket = Ticket(ticketId, showtimeId, seatId, customerId, price, TicketStatus.Purchased, purchasedAt)
      
      ticket.id shouldBe ticketId
      ticket.showtimeId shouldBe showtimeId
      ticket.seatId shouldBe seatId
      ticket.customerId shouldBe customerId
      ticket.price shouldBe price
      ticket.status shouldBe TicketStatus.Purchased
      ticket.purchasedAt shouldBe purchasedAt
    }
  }

  "Customer" should {
    "be created with all fields" in {
      val customerId = CustomerId(UUID.randomUUID())
      val customer = Customer(customerId, "test@example.com", "John", "Doe")
      
      customer.id shouldBe customerId
      customer.email shouldBe "test@example.com"
      customer.firstName shouldBe "John"
      customer.lastName shouldBe "Doe"
    }
  }

  "CreateReservationRequest" should {
    "be created with all fields" in {
      val showtimeId = ShowtimeId(UUID.randomUUID())
      val seatIds = List(SeatId("A1-1"), SeatId("A1-2"))
      val customerId = CustomerId(UUID.randomUUID())
      
      val request = CreateReservationRequest(showtimeId, seatIds, customerId)
      
      request.showtimeId shouldBe showtimeId
      request.seatIds shouldBe seatIds
      request.customerId shouldBe customerId
    }
  }

  "ReservationResponse" should {
    "be created with all fields" in {
      val tickets = List.empty[Ticket]
      val totalPrice = BigDecimal("50.00")
      
      val response = ReservationResponse(tickets, totalPrice)
      
      response.tickets shouldBe tickets
      response.totalPrice shouldBe totalPrice
    }
  }

  "AvailableSeatsResponse" should {
    "be created with all fields" in {
      val showtimeId = ShowtimeId(UUID.randomUUID())
      val seats = List.empty[Seat]
      
      val response = AvailableSeatsResponse(showtimeId, seats)
      
      response.showtimeId shouldBe showtimeId
      response.availableSeats shouldBe seats
    }
  }

  "DomainError" should {
    "MovieNotFound should have correct message" in {
      val movieId = MovieId(UUID.randomUUID())
      val error = DomainError.MovieNotFound(movieId)
      
      error.message shouldBe s"Movie with id ${movieId.value} not found"
      error.getMessage shouldBe error.message
    }

    "TheaterNotFound should have correct message" in {
      val theaterId = TheaterId(UUID.randomUUID())
      val error = DomainError.TheaterNotFound(theaterId)
      
      error.message shouldBe s"Theater with id ${theaterId.value} not found"
    }

    "ShowtimeNotFound should have correct message" in {
      val showtimeId = ShowtimeId(UUID.randomUUID())
      val error = DomainError.ShowtimeNotFound(showtimeId)
      
      error.message shouldBe s"Showtime with id ${showtimeId.value} not found"
    }

    "SeatNotAvailable should have correct message" in {
      val seatId = SeatId("A1-5")
      val error = DomainError.SeatNotAvailable(seatId)
      
      error.message shouldBe s"Seat ${seatId.value} is not available"
    }

    "CustomerNotFound should have correct message" in {
      val customerId = CustomerId(UUID.randomUUID())
      val error = DomainError.CustomerNotFound(customerId)
      
      error.message shouldBe s"Customer with id ${customerId.value} not found"
    }

    "TicketNotFound should have correct message" in {
      val ticketId = TicketId(UUID.randomUUID())
      val error = DomainError.TicketNotFound(ticketId)
      
      error.message shouldBe s"Ticket with id ${ticketId.value} not found"
    }

    "InvalidReservation should have correct message" in {
      val reason = "Test reason"
      val error = DomainError.InvalidReservation(reason)
      
      error.message shouldBe s"Invalid reservation: $reason"
    }
  }
} 