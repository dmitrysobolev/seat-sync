package com.movietheater.interpreters

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import java.util.UUID

class InMemoryTheaterAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "InMemoryTheaterAlgebra" - {
    "findById" - {
      "should return theater when it exists" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val theater = Theater(theaterId, "Test Theater", "Test Location", 100)
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO](Map(theaterId -> theater))
          result <- algebra.findById(theaterId)
        } yield result
        
        test.asserting(_ shouldBe Some(theater))
      }
      
      "should return None when theater doesn't exist" in {
        val theaterId = TheaterId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO]()
          result <- algebra.findById(theaterId)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findAll" - {
      "should return all theaters" in {
        val theater1 = Theater(TheaterId(UUID.randomUUID()), "Theater 1", "Location 1", 100)
        val theater2 = Theater(TheaterId(UUID.randomUUID()), "Theater 2", "Location 2", 150)
        val theaters = Map(theater1.id -> theater1, theater2.id -> theater2)
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO](theaters)
          result <- algebra.findAll()
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(theater1, theater2)
        }
      }
      
      "should return empty list when no theaters exist" in {
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO]()
          result <- algebra.findAll()
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "create" - {
      "should create and return new theater" in {
        val theater = Theater(TheaterId(UUID.randomUUID()), "New Theater", "New Location", 200)
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO]()
          result <- algebra.create(theater)
          retrieved <- algebra.findById(theater.id)
        } yield (result, retrieved)
        
        test.asserting { case (created, retrieved) =>
          created shouldBe theater
          retrieved shouldBe Some(theater)
        }
      }
    }
    
    "update" - {
      "should update existing theater" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val originalTheater = Theater(theaterId, "Original", "Original Location", 100)
        val updatedTheater = Theater(theaterId, "Updated", "Updated Location", 150)
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO](Map(theaterId -> originalTheater))
          result <- algebra.update(updatedTheater)
          retrieved <- algebra.findById(theaterId)
        } yield (result, retrieved)
        
        test.asserting { case (updated, retrieved) =>
          updated shouldBe Some(updatedTheater)
          retrieved shouldBe Some(updatedTheater)
        }
      }
      
      "should return None when theater doesn't exist" in {
        val theater = Theater(TheaterId(UUID.randomUUID()), "Non-existent", "Location", 100)
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO]()
          result <- algebra.update(theater)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "delete" - {
      "should delete existing theater" in {
        val theaterId = TheaterId(UUID.randomUUID())
        val theater = Theater(theaterId, "To Delete", "Location", 100)
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO](Map(theaterId -> theater))
          result <- algebra.delete(theaterId)
          retrieved <- algebra.findById(theaterId)
        } yield (result, retrieved)
        
        test.asserting { case (deleted, retrieved) =>
          deleted shouldBe true
          retrieved shouldBe None
        }
      }
      
      "should return false when theater doesn't exist" in {
        val theaterId = TheaterId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryTheaterAlgebra[IO]()
          result <- algebra.delete(theaterId)
        } yield result
        
        test.asserting(_ shouldBe false)
      }
    }
  }
} 