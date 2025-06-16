package com.movietheater.interpreters

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime

class InMemoryShowtimeAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "InMemoryShowtimeAlgebra" - {
    "findById" - {
      "should return showtime when it exists" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val showtime = Showtime(showtimeId, MovieId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
          result <- algebra.findById(showtimeId)
        } yield result
        
        test.asserting(_ shouldBe Some(showtime))
      }
      
      "should return None when showtime doesn't exist" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO]()
          result <- algebra.findById(showtimeId)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findByMovie" - {
      "should return showtimes for specific movie" in {
        val movieId1 = MovieId(UUID.randomUUID())
        val movieId2 = MovieId(UUID.randomUUID())
        val theaterId = TheaterId(UUID.randomUUID())
        val showtime1 = Showtime(ShowtimeId(UUID.randomUUID()), movieId1, theaterId, 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        val showtime2 = Showtime(ShowtimeId(UUID.randomUUID()), movieId1, theaterId, 
          LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), Money.fromDollars(15, 0))
        val showtime3 = Showtime(ShowtimeId(UUID.randomUUID()), movieId2, theaterId, 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(12, 0))
        val showtimes = Map(showtime1.id -> showtime1, showtime2.id -> showtime2, showtime3.id -> showtime3)
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO](showtimes)
          result <- algebra.findByMovie(movieId1)
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(showtime1, showtime2)
        }
      }
      
      "should return empty list when movie has no showtimes" in {
        val movieId = MovieId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO]()
          result <- algebra.findByMovie(movieId)
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "findByTheater" - {
      "should return showtimes for specific theater" in {
        val movieId = MovieId(UUID.randomUUID())
        val theaterId1 = TheaterId(UUID.randomUUID())
        val theaterId2 = TheaterId(UUID.randomUUID())
        val showtime1 = Showtime(ShowtimeId(UUID.randomUUID()), movieId, theaterId1, 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        val showtime2 = Showtime(ShowtimeId(UUID.randomUUID()), movieId, theaterId1, 
          LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), Money.fromDollars(15, 0))
        val showtime3 = Showtime(ShowtimeId(UUID.randomUUID()), movieId, theaterId2, 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(12, 0))
        val showtimes = Map(showtime1.id -> showtime1, showtime2.id -> showtime2, showtime3.id -> showtime3)
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO](showtimes)
          result <- algebra.findByTheater(theaterId1)
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(showtime1, showtime2)
        }
      }
      
      "should return empty list when theater has no showtimes" in {
        val theaterId = TheaterId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO]()
          result <- algebra.findByTheater(theaterId)
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "create" - {
      "should create and return new showtime" in {
        val showtime = Showtime(ShowtimeId(UUID.randomUUID()), MovieId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO]()
          result <- algebra.create(showtime)
          retrieved <- algebra.findById(showtime.id)
        } yield (result, retrieved)
        
        test.asserting { case (created, retrieved) =>
          created shouldBe showtime
          retrieved shouldBe Some(showtime)
        }
      }
    }
    
    "update" - {
      "should update existing showtime" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val originalShowtime = Showtime(showtimeId, MovieId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        val updatedShowtime = originalShowtime.copy(price = Money.fromDollars(20, 0))
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> originalShowtime))
          result <- algebra.update(updatedShowtime)
          retrieved <- algebra.findById(showtimeId)
        } yield (result, retrieved)
        
        test.asserting { case (updated, retrieved) =>
          updated shouldBe Some(updatedShowtime)
          retrieved shouldBe Some(updatedShowtime)
        }
      }
      
      "should return None when showtime doesn't exist" in {
        val showtime = Showtime(ShowtimeId(UUID.randomUUID()), MovieId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO]()
          result <- algebra.update(showtime)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "delete" - {
      "should delete existing showtime" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        val showtime = Showtime(showtimeId, MovieId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), 
          LocalDateTime.now(), LocalDateTime.now().plusHours(2), Money.fromDollars(15, 0))
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO](Map(showtimeId -> showtime))
          result <- algebra.delete(showtimeId)
          retrieved <- algebra.findById(showtimeId)
        } yield (result, retrieved)
        
        test.asserting { case (deleted, retrieved) =>
          deleted shouldBe true
          retrieved shouldBe None
        }
      }
      
      "should return false when showtime doesn't exist" in {
        val showtimeId = ShowtimeId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryShowtimeAlgebra[IO]()
          result <- algebra.delete(showtimeId)
        } yield result
        
        test.asserting(_ shouldBe false)
      }
    }
  }
} 