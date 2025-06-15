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
        val ticket = Ticket(
          ticketId,
          ShowtimeId(UUID.randomUUID()),
          SeatId("A1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> ticket))
          result <- algebra.findById(ticketId)
        } yield result

        test.asserting { result =>
          result shouldBe Some(ticket)
        }
      }

      "should return None when ticket doesn't exist" in {
        val ticketId = TicketId(UUID.randomUUID())

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findById(ticketId)
        } yield result

        test.asserting { result =>
          result shouldBe None
        }
      }
    }

    "findByCustomer" - {
      "should return tickets for specific customer" in {
        val customerId = CustomerId(UUID.randomUUID())
        val ticket1 = Ticket(
          TicketId(UUID.randomUUID()),
          ShowtimeId(UUID.randomUUID()),
          SeatId("A1-1"),
          customerId,
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )
        val ticket2 = Ticket(
          TicketId(UUID.randomUUID()),
          ShowtimeId(UUID.randomUUID()),
          SeatId("A1-2"),
          customerId,
          BigDecimal("15.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )
        val otherTicket = Ticket(
          TicketId(UUID.randomUUID()),
          ShowtimeId(UUID.randomUUID()),
          SeatId("B1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("20.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(
            ticket1.id -> ticket1,
            ticket2.id -> ticket2,
            otherTicket.id -> otherTicket
          ))
          result <- algebra.findByCustomer(customerId)
        } yield result

        test.asserting { tickets =>
          tickets should have length 2
          tickets should contain theSameElementsAs List(ticket1, ticket2)
        }
      }

      "should return empty list when customer has no tickets" in {
        val customerId = CustomerId(UUID.randomUUID())

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findByCustomer(customerId)
        } yield result

        test.asserting { tickets =>
          tickets shouldBe empty
        }
      }
    }

    "findByShowtime" - {
      "should return tickets for specific showtime" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val ticket1 = Ticket(
          TicketId(UUID.randomUUID()),
          showtimeId,
          SeatId("A1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )
        val ticket2 = Ticket(
          TicketId(UUID.randomUUID()),
          showtimeId,
          SeatId("A1-2"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("15.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )
        val otherTicket = Ticket(
          TicketId(UUID.randomUUID()),
          ShowtimeId(UUID.randomUUID()),
          SeatId("B1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("20.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(
            ticket1.id -> ticket1,
            ticket2.id -> ticket2,
            otherTicket.id -> otherTicket
          ))
          result <- algebra.findByShowtime(showtimeId)
        } yield result

        test.asserting { tickets =>
          tickets should have length 2
          tickets should contain theSameElementsAs List(ticket1, ticket2)
        }
      }

      "should return empty list when showtime has no tickets" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findByShowtime(showtimeId)
        } yield result

        test.asserting { tickets =>
          tickets shouldBe empty
        }
      }
    }

    "findBySeatAndShowtime" - {
      "should return ticket when it exists" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val ticket = Ticket(
          TicketId(UUID.randomUUID()),
          showtimeId,
          seatId,
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticket.id -> ticket))
          result <- algebra.findBySeatAndShowtime(seatId, showtimeId)
        } yield result

        test.asserting { result =>
          result shouldBe Some(ticket)
        }
      }

      "should return None when ticket doesn't exist" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val seatId = SeatId("A1-1")

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.findBySeatAndShowtime(seatId, showtimeId)
        } yield result

        test.asserting { result =>
          result shouldBe None
        }
      }

      "should return None when seat exists but for different showtime" in {
        val showtimeId1 = ShowtimeId(UUID.randomUUID())
        val showtimeId2 = ShowtimeId(UUID.randomUUID())
        val seatId = SeatId("A1-1")
        val ticket = Ticket(
          TicketId(UUID.randomUUID()),
          showtimeId1,
          seatId,
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticket.id -> ticket))
          result <- algebra.findBySeatAndShowtime(seatId, showtimeId2)
        } yield result

        test.asserting { result =>
          result shouldBe None
        }
      }

      "should return None when showtime exists but for different seat" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val seatId1 = SeatId("A1-1")
        val seatId2 = SeatId("A1-2")
        val ticket = Ticket(
          TicketId(UUID.randomUUID()),
          showtimeId,
          seatId1,
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticket.id -> ticket))
          result <- algebra.findBySeatAndShowtime(seatId2, showtimeId)
        } yield result

        test.asserting { result =>
          result shouldBe None
        }
      }
    }

    "create" - {
      "should create and return new ticket" in {
        val ticket = Ticket(
          TicketId(UUID.randomUUID()),
          ShowtimeId(UUID.randomUUID()),
          SeatId("A1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          created <- algebra.create(ticket)
          retrieved <- algebra.findById(ticket.id)
        } yield (created, retrieved)

        test.asserting { case (created, retrieved) =>
          created shouldBe ticket
          retrieved shouldBe Some(ticket)
        }
      }
    }

    "updateStatus" - {
      "should update existing ticket status" in {
        val ticketId = TicketId(UUID.randomUUID())
        val originalTicket = Ticket(
          ticketId,
          ShowtimeId(UUID.randomUUID()),
          SeatId("A1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )
        val newStatus = TicketStatus.Purchased

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO](Map(ticketId -> originalTicket))
          result <- algebra.updateStatus(ticketId, newStatus)
          updated <- algebra.findById(ticketId)
        } yield (result, updated)

        test.asserting { case (result, updated) =>
          result shouldBe Some(originalTicket.copy(status = newStatus))
          updated shouldBe Some(originalTicket.copy(status = newStatus))
        }
      }

      "should return None when ticket doesn't exist" in {
        val ticketId = TicketId(UUID.randomUUID())
        val newStatus = TicketStatus.Purchased

        val test = for {
          algebra <- InMemoryTicketAlgebra[IO]()
          result <- algebra.updateStatus(ticketId, newStatus)
        } yield result

        test.asserting { result =>
          result shouldBe None
        }
      }
    }

    "delete" - {
      "should delete existing ticket" in {
        val ticketId = TicketId(UUID.randomUUID())
        val ticket = Ticket(
          ticketId,
          ShowtimeId(UUID.randomUUID()),
          SeatId("A1-1"),
          CustomerId(UUID.randomUUID()),
          BigDecimal("10.00"),
          TicketStatus.Reserved,
          LocalDateTime.now()
        )

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

        test.asserting { result =>
          result shouldBe false
        }
      }
    }
  }
} 