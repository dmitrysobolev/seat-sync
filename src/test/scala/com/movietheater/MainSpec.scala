package com.movietheater

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._

class MainSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "Main" - {
    "createServer" - {
      "should create server resource successfully" in {
        val test = Main.createServer[IO].use { server =>
          IO {
            server should not be null
            val address = server.address
            address.getPort shouldBe 8080
            // Address can be either IPv4 "0.0.0.0" or IPv6 "0:0:0:0:0:0:0:0"
            val hostString = address.getHostString
            hostString should (be("0.0.0.0") or include("0:0:0:0"))
          }
        }
        
        test.assertNoException
      }
    }
    
    "createSampleData" - {
      "should create complete sample data set" in {
        val test = Main.createSampleData[IO].map { case (movies, theaters, showtimes, seats, tickets, customers) =>
          // Verify movies
          movies.size shouldBe 2
          movies.values.map(_.title) should contain allOf ("The Matrix", "Inception")
          
          // Verify theaters  
          theaters.size shouldBe 2
          theaters.values.map(_.name) should contain allOf ("Cinema One", "Grand Theater")
          theaters.values.foreach(_.totalSeats should be > 0)
          
          // Verify seats
          seats.size shouldBe (10 * 10 + 15 * 10) // Cinema One (10x10) + Grand Theater (15x10)
          seats.size shouldBe 250
          
          val cinemaOneSeats = seats.values.filter(_.id.value.startsWith("A"))
          val grandTheaterSeats = seats.values.filter(_.id.value.startsWith("B"))
          cinemaOneSeats.size shouldBe 100
          grandTheaterSeats.size shouldBe 150
          
          // Verify seat types distribution
          val vipSeats = seats.values.filter(_.seatType == SeatType.VIP)
          val premiumSeats = seats.values.filter(_.seatType == SeatType.Premium)  
          val regularSeats = seats.values.filter(_.seatType == SeatType.Regular)
          
          vipSeats.size should be > 0
          premiumSeats.size should be > 0
          regularSeats.size should be > 0
          
          // Verify showtimes
          showtimes.size shouldBe 3
          showtimes.values.foreach { showtime =>
            showtime.startTime should be < showtime.endTime
            showtime.price should be > BigDecimal(0)
          }
          
          // Verify customers
          customers.size shouldBe 2
          customers.values.map(_.email) should contain allOf ("john@example.com", "jane@example.com")
          customers.values.map(_.firstName) should contain allOf ("John", "Jane")
          
          // Verify no tickets initially
          tickets shouldBe empty
        }
        
        test.assertNoException
      }
      
      "should create seats with correct theater assignment" in {
        val test = Main.createSampleData[IO].map { case (_, theaters, _, seats, _, _) =>
          val theaterIds = theaters.keys.toSet
          
          // All seats should belong to one of the theaters
          seats.values.foreach { seat =>
            theaterIds should contain(seat.theaterId)
          }
          
          // Each theater should have seats assigned
          theaterIds.foreach { theaterId =>
            val theaterSeats = seats.values.filter(_.theaterId == theaterId)
            theaterSeats should not be empty
          }
        }
        
        test.assertNoException
      }
      
      "should create showtimes with valid theater and movie references" in {
        val test = Main.createSampleData[IO].map { case (movies, theaters, showtimes, _, _, _) =>
          val movieIds = movies.keys.toSet
          val theaterIds = theaters.keys.toSet
          
          showtimes.values.foreach { showtime =>
            movieIds should contain(showtime.movieId)
            theaterIds should contain(showtime.theaterId)
          }
        }
        
        test.assertNoException
      }
      
      "should create unique IDs for all entities" in {
        val test = Main.createSampleData[IO].map { case (movies, theaters, showtimes, seats, _, customers) =>
          // Verify unique movie IDs
          movies.keys.toSet.size shouldBe movies.size
          
          // Verify unique theater IDs  
          theaters.keys.toSet.size shouldBe theaters.size
          
          // Verify unique showtime IDs
          showtimes.keys.toSet.size shouldBe showtimes.size
          
          // Verify unique seat IDs
          seats.keys.toSet.size shouldBe seats.size
          
          // Verify unique customer IDs
          customers.keys.toSet.size shouldBe customers.size
          
          // Verify unique customer emails
          customers.values.map(_.email).toSet.size shouldBe customers.size
        }
        
        test.assertNoException
      }
    }
  }
} 