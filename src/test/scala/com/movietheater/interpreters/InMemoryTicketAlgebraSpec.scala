package com.movietheater.interpreters

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime

class InMemoryTicketAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "InMemoryTicketAlgebra" - {
    "findById" - {
      "should return ticket when it exists" in {
        val ticketId = TicketId(UUID.randomUUID())
        val ticket = Ticket(ticketId, ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), CustomerId(UUID.randomUUID()), 
          BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> ticket))
          result <- algebra.findById(ticketId)
        } yield result
        
        test.asserting(_ shouldBe Some(ticket))
      }
      
      "should return None when ticket doesn't exist" in {
        val ticketId = TicketId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findById(ticketId)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findByCustomer" - {
      "should return tickets for specific customer" in {
        val customerId1 = CustomerId(UUID.randomUUID())
        val customerId2 = CustomerId(UUID.randomUUID())
        val ticket1 = Ticket(TicketId(UUID.randomUUID()), ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), customerId1, 
          BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        val ticket2 = Ticket(TicketId(UUID.randomUUID()), ShowtimeId(UUID.randomUUID()), SeatId("A1-2"), customerId1, 
          BigDecimal("20.00"), TicketStatus.Purchased, LocalDateTime.now())
        val ticket3 = Ticket(TicketId(UUID.randomUUID()), ShowtimeId(UUID.randomUUID()), SeatId("B1-1"), customerId2, 
          BigDecimal("12.00"), TicketStatus.Reserved, LocalDateTime.now())
        val tickets = Map(ticket1.id -> ticket1, ticket2.id -> ticket2, ticket3.id -> ticket3)
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](tickets)
          result <- algebra.findByCustomer(customerId1)
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(ticket1, ticket2)
        }
      }
      
      "should return empty list when customer has no tickets" in {
        val customerId = CustomerId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findByCustomer(customerId)
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "findByShowtime" - {
      "should return tickets for specific showtime" in {
        val showtimeId1 = ShowtimeId(UUID.randomUUID())
        val showtimeId2 = ShowtimeId(UUID.randomUUID())
        val ticket1 = Ticket(TicketId(UUID.randomUUID()), showtimeId1, SeatId("A1-1"), CustomerId(UUID.randomUUID()), 
          BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        val ticket2 = Ticket(TicketId(UUID.randomUUID()), showtimeId1, SeatId("A1-2"), CustomerId(UUID.randomUUID()), 
          BigDecimal("20.00"), TicketStatus.Purchased, LocalDateTime.now())
        val ticket3 = Ticket(TicketId(UUID.randomUUID()), showtimeId2, SeatId("B1-1"), CustomerId(UUID.randomUUID()), 
          BigDecimal("12.00"), TicketStatus.Reserved, LocalDateTime.now())
        val tickets = Map(ticket1.id -> ticket1, ticket2.id -> ticket2, ticket3.id -> ticket3)
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](tickets)
          result <- algebra.findByShowtime(showtimeId1)
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(ticket1, ticket2)
        }
      }
      
      "should return empty list when showtime has no tickets" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findByShowtime(showtimeId)
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "create" - {
      "should create and return new ticket" in {
        val ticket = Ticket(TicketId(UUID.randomUUID()), ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), CustomerId(UUID.randomUUID()), 
          BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.create(ticket)
          retrieved <- algebra.findById(ticket.id)
        } yield (result, retrieved)
        
        test.asserting { case (created, retrieved) =>
          created shouldBe ticket
          retrieved shouldBe Some(ticket)
        }
      }
    }
    
    "updateStatus" - {
      "should update existing ticket status" in {
        val ticketId = TicketId(UUID.randomUUID())
        val originalTicket = Ticket(ticketId, ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), CustomerId(UUID.randomUUID()), 
          BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> originalTicket))
          result <- algebra.updateStatus(ticketId, TicketStatus.Purchased)
          retrieved <- algebra.findById(ticketId)
        } yield (result, retrieved)
        
        test.asserting { case (updated, retrieved) =>
          updated.isDefined shouldBe true
          updated.get.status shouldBe TicketStatus.Purchased
          retrieved.isDefined shouldBe true
          retrieved.get.status shouldBe TicketStatus.Purchased
        }
      }
      
      "should return None when ticket doesn't exist" in {
        val ticketId = TicketId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.updateStatus(ticketId, TicketStatus.Purchased)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "delete" - {
      "should delete existing ticket" in {
        val ticketId = TicketId(UUID.randomUUID())
        val ticket = Ticket(ticketId, ShowtimeId(UUID.randomUUID()), SeatId("A1-1"), CustomerId(UUID.randomUUID()), 
          BigDecimal("15.00"), TicketStatus.Reserved, LocalDateTime.now())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> ticket))
          result <- algebra.delete(ticketId)
          retrieved <- algebra.findById(ticketId)
        } yield (result, retrieved)
        
        test.asserting { case (deleted, retrieved) =>
          deleted shouldBe true
          retrieved shouldBe None
        }
      }
      
      "should return false when ticket doesn't exist" in {
        val ticketId = TicketId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.delete(ticketId)
        } yield result
        
        test.asserting(_ shouldBe false)
      }
    }
  }
} 