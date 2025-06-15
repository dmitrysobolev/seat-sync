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
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")

        val seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Regular)

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId1 -> seat1, seatId2 -> seat2))
          availableSeats <- seatAlgebra.findAvailableForShowtime(showtimeId)
        } yield availableSeats

        test.asserting { seats =>
          seats should have length 2
          seats should contain theSameElementsAs List(seat1, seat2)
        }
      }
      
      "should exclude seats with tickets" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val customerId = CustomerId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")

        val seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Regular)
        val ticket = Ticket(
          TicketId(UUID.randomUUID()),
          showtimeId,
          seatId1,
          customerId,
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          java.time.LocalDateTime.now()
        )

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO](Map(ticket.id -> ticket))
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId1 -> seat1, seatId2 -> seat2))
          availableSeats <- seatAlgebra.findAvailableForShowtime(showtimeId)
        } yield availableSeats

        test.asserting { seats =>
          seats should have length 1
          seats should contain only seat2
        }
      }
      
      "should return empty list when no seats exist for showtime" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          availableSeats <- seatAlgebra.findAvailableForShowtime(showtimeId)
        } yield availableSeats
        
        test.asserting { seats =>
          seats shouldBe empty
        }
      }
    }
    
    "findById" - {
      "should return seat when it exists" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val seat = Seat(seatId, "A1", 1, theaterId, SeatType.Regular)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId -> seat))
          result <- seatAlgebra.findById(seatId)
        } yield result
        
        test.asserting { result =>
          result shouldBe Some(seat)
        }
      }
      
      "should return None when seat doesn't exist" in {
        val seatId = SeatId("A1-1")
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- seatAlgebra.findById(seatId)
        } yield result
        
        test.asserting { result =>
          result shouldBe None
        }
      }
    }
    
    "findByTheater" - {
      "should return seats for specific theater" in {
        val theaterId1 = TheaterId(UUID.randomUUID())
        val theaterId2 = TheaterId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")
        val seatId3 = SeatId("B1-1")

        val seat1 = Seat(seatId1, "A1", 1, theaterId1, SeatType.Regular)
        val seat2 = Seat(seatId2, "A1", 2, theaterId1, SeatType.Regular)
        val seat3 = Seat(seatId3, "B1", 1, theaterId2, SeatType.Regular)

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(
            seatId1 -> seat1,
            seatId2 -> seat2,
            seatId3 -> seat3
          ))
          result <- seatAlgebra.findByTheater(theaterId1)
        } yield result
        
        test.asserting { seats =>
          seats should have length 2
          seats should contain theSameElementsAs List(seat1, seat2)
        }
      }
      
      "should return empty list when theater has no seats" in {
        val theaterId = TheaterId(UUID.randomUUID())
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- seatAlgebra.findByTheater(theaterId)
        } yield result
        
        test.asserting { seats =>
          seats shouldBe empty
        }
      }
    }
    
    "create" - {
      "should create and return new seat" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val seat = Seat(seatId, "A1", 1, theaterId, SeatType.Regular)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          created <- seatAlgebra.create(seat)
          allSeats <- seatAlgebra.findByTheater(theaterId)
        } yield (created, allSeats)
        
        test.asserting { case (created, allSeats) =>
          created shouldBe seat
          allSeats should have length 1
          allSeats should contain only seat
        }
      }
    }
    
    "createMany" - {
      "should create multiple seats" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")
        val seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Regular)

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          created <- seatAlgebra.createMany(List(seat1, seat2))
          allSeats <- seatAlgebra.findByTheater(theaterId)
        } yield (created, allSeats)

        test.asserting { case (created, allSeats) =>
          created should have length 2
          created should contain theSameElementsAs List(seat1, seat2)
          allSeats should have length 2
          allSeats should contain theSameElementsAs List(seat1, seat2)
        }
      }
    }
    
    "update" - {
      "should update existing seat" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val originalSeat = Seat(seatId, "A1", 1, theaterId, SeatType.Regular)
        val updatedSeat = originalSeat.copy(seatType = SeatType.Premium)

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId -> originalSeat))
          result <- seatAlgebra.update(updatedSeat)
          allSeats <- seatAlgebra.findByTheater(theaterId)
        } yield (result, allSeats)

        test.asserting { case (result, allSeats) =>
          result shouldBe Some(updatedSeat)
          allSeats should have length 1
          allSeats should contain only updatedSeat
        }
      }
      
      "should return None when seat doesn't exist" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val seat = Seat(seatId, "A1", 1, theaterId, SeatType.Regular)

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- seatAlgebra.update(seat)
        } yield result

        test.asserting { result =>
          result shouldBe None
        }
      }
    }
    
    "delete" - {
      "should delete existing seat" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val seat = Seat(seatId, "A1", 1, theaterId, SeatType.Regular)
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(seatId -> seat))
          result <- seatAlgebra.delete(seatId)
          allSeats <- seatAlgebra.findByTheater(theaterId)
        } yield (result, allSeats)
        
        test.asserting { case (result, allSeats) =>
          result shouldBe true
          allSeats shouldBe empty
        }
      }
      
      "should return false when seat doesn't exist" in {
        val seatId = SeatId("A1-1")
        
        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
          result <- seatAlgebra.delete(seatId)
        } yield result
        
        test.asserting { result =>
          result shouldBe false
        }
      }
    }
    
    "deleteAll" - {
      "should delete all seats" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")
        val seat1 = Seat(seatId1, "A1", 1, theaterId, SeatType.Regular)
        val seat2 = Seat(seatId2, "A1", 2, theaterId, SeatType.Regular)

        val test = for {
          ticketAlgebra <- InMemoryTicketAlgebra[IO]()
          seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra, Map(
            seatId1 -> seat1,
            seatId2 -> seat2
          ))
          _ <- seatAlgebra.deleteAll()
          allSeats <- seatAlgebra.findByTheater(theaterId)
        } yield allSeats

        test.asserting { seats =>
          seats shouldBe empty
        }
      }
    }
  }
} 