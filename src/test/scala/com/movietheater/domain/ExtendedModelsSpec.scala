package com.movietheater.domain

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.util.UUID
import java.time.LocalDateTime

class ExtendedModelsSpec extends AnyWordSpec with Matchers {

  "Value classes equality and hashCode" should {
    "work correctly for MovieId" in {
      val uuid = UUID.randomUUID()
      val id1 = MovieId(uuid)
      val id2 = MovieId(uuid)
      val id3 = MovieId(UUID.randomUUID())
      
      id1 shouldBe id2
      id1.hashCode() shouldBe id2.hashCode()
      id1 should not be id3
    }
    
    "work correctly for TheaterId" in {
      val uuid = UUID.randomUUID()
      val id1 = TheaterId(uuid)
      val id2 = TheaterId(uuid)
      val id3 = TheaterId(UUID.randomUUID())
      
      id1 shouldBe id2
      id1.hashCode() shouldBe id2.hashCode()
      id1 should not be id3
    }
    
    "work correctly for SeatId with String values" in {
      val id1 = SeatId("A1-5")
      val id2 = SeatId("A1-5")
      val id3 = SeatId("B2-3")
      
      id1 shouldBe id2
      id1.hashCode() shouldBe id2.hashCode()
      id1 should not be id3
    }
  }

  "SeatType enum" should {
    "have all expected values" in {
      val allTypes = List(SeatType.Regular, SeatType.Premium, SeatType.VIP)
      allTypes.size shouldBe 3
      allTypes should contain allOf (SeatType.Regular, SeatType.Premium, SeatType.VIP)
    }
    
    "support equality" in {
      SeatType.Regular shouldBe SeatType.Regular
      SeatType.Premium shouldBe SeatType.Premium  
      SeatType.VIP shouldBe SeatType.VIP
      
      SeatType.Regular should not be SeatType.Premium
      SeatType.Premium should not be SeatType.VIP
    }
    
    "support pattern matching" in {
      val seatType: SeatType = SeatType.VIP
      
      val result = seatType match {
        case SeatType.Regular => "regular"
        case SeatType.Premium => "premium"
        case SeatType.VIP => "vip"
      }
      
      result shouldBe "vip"
    }
  }

  "TicketStatus enum" should {
    "have all expected values" in {
      val allStatuses = List(TicketStatus.Reserved, TicketStatus.Purchased, TicketStatus.Cancelled)
      allStatuses.size shouldBe 3
      allStatuses should contain allOf (TicketStatus.Reserved, TicketStatus.Purchased, TicketStatus.Cancelled)
    }
    
    "support equality" in {
      TicketStatus.Reserved shouldBe TicketStatus.Reserved
      TicketStatus.Purchased shouldBe TicketStatus.Purchased
      TicketStatus.Cancelled shouldBe TicketStatus.Cancelled
      
      TicketStatus.Reserved should not be TicketStatus.Purchased
      TicketStatus.Purchased should not be TicketStatus.Cancelled
    }
    
    "support pattern matching" in {
      val status: TicketStatus = TicketStatus.Purchased
      
      val result = status match {
        case TicketStatus.Reserved => "reserved"
        case TicketStatus.Purchased => "purchased"
        case TicketStatus.Cancelled => "cancelled"
      }
      
      result shouldBe "purchased"
    }
  }

  "DomainError hierarchy" should {
    "ShowtimeNotFound should have correct message" in {
      val error = DomainError.ShowtimeNotFound(ShowtimeId(UUID.randomUUID()))
      error.message should include("Showtime")
      error.message should include("not found")
      error.getMessage shouldBe error.message
      error shouldBe a[DomainError]
      error shouldBe a[Throwable]
    }
    
    "CustomerNotFound should have correct message" in {
      val error = DomainError.CustomerNotFound(CustomerId(UUID.randomUUID()))
      error.message should include("Customer")
      error.message should include("not found")
      error.getMessage shouldBe error.message
      error shouldBe a[DomainError]
    }
    
    "InvalidReservation should have correct message" in {
      val error = DomainError.InvalidReservation("Test error message")
      error.message should include("Test error message")
      error.getMessage shouldBe error.message
      error shouldBe a[DomainError]
    }
    
    "TicketNotFound should have correct message" in {
      val error = DomainError.TicketNotFound(TicketId(UUID.randomUUID()))
      error.message should include("Ticket")
      error.message should include("not found")
      error.getMessage shouldBe error.message
      error shouldBe a[DomainError]
    }
    
    "all error types should extend Throwable" in {
      val errors: List[DomainError] = List(
        DomainError.ShowtimeNotFound(ShowtimeId(UUID.randomUUID())),
        DomainError.CustomerNotFound(CustomerId(UUID.randomUUID())),
        DomainError.InvalidReservation("test"),
        DomainError.TicketNotFound(TicketId(UUID.randomUUID()))
      )
      
      errors.foreach { error =>
        error shouldBe a[Throwable]
        error.message should not be empty
      }
    }
  }

  "Domain models edge cases" should {
    "Movie should handle edge values" in {
      val movie = Movie(
        MovieId(UUID.randomUUID()),
        "",  // empty title
        "",  // empty description  
        0,   // zero duration
        ""   // empty rating
      )
      
      movie.title shouldBe ""
      movie.description shouldBe ""
      movie.durationMinutes shouldBe 0
      movie.rating shouldBe ""
    }
    
    "Theater should handle edge values" in {
      val theater = Theater(
        TheaterId(UUID.randomUUID()),
        "",  // empty name
        "",  // empty location
        0    // zero seats
      )
      
      theater.name shouldBe ""
      theater.location shouldBe ""
      theater.totalSeats shouldBe 0
    }
    
    "Seat should handle minimum values" in {
      val seat = Seat(
        SeatId(""),
        "",  // empty row
        0,   // zero number
        TheaterId(UUID.randomUUID()),
        SeatType.Regular
      )
      
      seat.row shouldBe ""
      seat.number shouldBe 0
    }
    
    "Customer should handle edge values" in {
      val customer = Customer(
        CustomerId(UUID.randomUUID()),
        "",  // empty email
        "",  // empty first name
        ""   // empty last name
      )
      
      customer.email shouldBe ""
      customer.firstName shouldBe ""
      customer.lastName shouldBe ""
    }
    
    "Showtime should handle same start and end time" in {
      val time = LocalDateTime.now()
      val showtime = Showtime(
        ShowtimeId(UUID.randomUUID()),
        MovieId(UUID.randomUUID()),
        TheaterId(UUID.randomUUID()),
        time,
        time,  // same as start time
        Money.zero  // zero price
      )
      
      showtime.startTime shouldBe showtime.endTime
      showtime.price shouldBe Money.zero
    }
    
    "Ticket should handle zero price" in {
      val ticket = Ticket(
        TicketId(UUID.randomUUID()),
        ShowtimeId(UUID.randomUUID()),
        SeatId(""),
        CustomerId(UUID.randomUUID()),
        Money.zero,  // zero price
        TicketStatus.Reserved,
        LocalDateTime.now()
      )
      
      ticket.price shouldBe Money.zero
    }
  }

  "Request/Response models edge cases" should {
    "CreateReservationRequest should handle empty seat list" in {
      val request = CreateReservationRequest(
        ShowtimeId(UUID.randomUUID()),
        List.empty,  // empty seats
        CustomerId(UUID.randomUUID())
      )
      
      request.seatIds shouldBe empty
    }
    
    "ReservationResponse should handle empty tickets and zero price" in {
      val response = ReservationResponse(
        List.empty,   // empty tickets
        Money.zero // zero price
      )
      
      response.tickets shouldBe empty
      response.totalPrice shouldBe Money.zero
    }
    
    "AvailableSeatsResponse should handle empty seats" in {
      val response = AvailableSeatsResponse(
        ShowtimeId(UUID.randomUUID()),
        List.empty  // empty seats
      )
      
      response.availableSeats shouldBe empty
    }
  }
} 