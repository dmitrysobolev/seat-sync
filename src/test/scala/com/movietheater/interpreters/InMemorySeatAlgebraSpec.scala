package com.movietheater.interpreters

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime

class InMemorySeatAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "InMemorySeatAlgebra" - {
    "findAvailableForShowtime" - {
      "should return available seats when no tickets exist" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val seat1 = Seat(SeatId("A1-1"), "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(SeatId("A1-2"), "A1", 2, theaterId, SeatType.Premium)
        val seats = Map(seat1.id -> seat1, seat2.id -> seat2)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra, seats)
          result <- algebra.findAvailableForShowtime(showtimeId)
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(seat1, seat2)
        }
      }
      
      "should exclude seats with tickets" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val seat1 = Seat(SeatId("A1-1"), "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(SeatId("A1-2"), "A1", 2, theaterId, SeatType.Premium)
        val seats = Map(seat1.id -> seat1, seat2.id -> seat2)
        
        val ticket = Ticket(TicketId(UUID.randomUUID()), showtimeId, seat1.id, customerId, BigDecimal("10.00"), TicketStatus.Reserved, LocalDateTime.now())
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO](Map(ticket.id -> ticket))
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra, seats)
          result <- algebra.findAvailableForShowtime(showtimeId)
        } yield result
        
        test.asserting { result =>
          result shouldBe List(seat2)
        }
      }
      
      "should return empty list when no seats exist for showtime" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- algebra.findAvailableForShowtime(showtimeId)
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "findById" - {
      "should return seat when it exists" in {
        val seatId = SeatId("A1-1")
        val seat = Seat(seatId, "A1", 1, TheaterId(UUID.randomUUID()), SeatType.Regular)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId -> seat))
          result <- algebra.findById(seatId)
        } yield result
        
        test.asserting(_ shouldBe Some(seat))
      }
      
      "should return None when seat doesn't exist" in {
        val seatId = SeatId("A1-1")
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- algebra.findById(seatId)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findByTheater" - {
      "should return seats for specific theater" in {
        val theaterId1 = TheaterId(UUID.randomUUID())
        val theaterId2 = TheaterId(UUID.randomUUID())
        val seat1 = Seat(SeatId("A1-1"), "A1", 1, theaterId1, SeatType.Regular)
        val seat2 = Seat(SeatId("A1-2"), "A1", 2, theaterId1, SeatType.Premium)
        val seat3 = Seat(SeatId("B1-1"), "B1", 1, theaterId2, SeatType.VIP)
        val seats = Map(seat1.id -> seat1, seat2.id -> seat2, seat3.id -> seat3)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra, seats)
          result <- algebra.findByTheater(theaterId1)
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(seat1, seat2)
        }
      }
      
      "should return empty list when theater has no seats" in {
        val theaterId = TheaterId(UUID.randomUUID())
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- algebra.findByTheater(theaterId)
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "create" - {
      "should create and return new seat" in {
        val seat = Seat(SeatId("A1-1"), "A1", 1, TheaterId(UUID.randomUUID()), SeatType.Regular)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- algebra.create(seat)
          retrieved <- algebra.findById(seat.id)
        } yield (result, retrieved)
        
        test.asserting { case (created, retrieved) =>
          created shouldBe seat
          retrieved shouldBe Some(seat)
        }
      }
    }
    
    "delete" - {
      "should delete existing seat" in {
        val seatId = SeatId("A1-1")
        val seat = Seat(seatId, "A1", 1, TheaterId(UUID.randomUUID()), SeatType.Regular)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId -> seat))
          result <- algebra.delete(seatId)
          retrieved <- algebra.findById(seatId)
        } yield (result, retrieved)
        
        test.asserting { case (deleted, retrieved) =>
          deleted shouldBe true
          retrieved shouldBe None
        }
      }
      
      "should return false when seat doesn't exist" in {
        val seatId = SeatId("A1-1")
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          algebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- algebra.delete(seatId)
        } yield result
        
        test.asserting(_ shouldBe false)
      }
    }
  }
} 