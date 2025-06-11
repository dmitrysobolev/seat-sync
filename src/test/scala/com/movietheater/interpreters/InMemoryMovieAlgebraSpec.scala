package com.movietheater.interpreters

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import java.util.UUID

class InMemoryMovieAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "InMemoryMovieAlgebra" - {
    "findById" - {
      "should return movie when it exists" in {
        val movieId = MovieId(UUID.randomUUID())
        val movie = Movie(movieId, "Test Movie", "Description", 120, "PG")
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO](Map(movieId -> movie))
          result <- algebra.findById(movieId)
        } yield result
        
        test.asserting(_ shouldBe Some(movie))
      }
      
      "should return None when movie doesn't exist" in {
        val movieId = MovieId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO]()
          result <- algebra.findById(movieId)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findAll" - {
      "should return all movies" in {
        val movie1 = Movie(MovieId(UUID.randomUUID()), "Movie 1", "Desc 1", 90, "G")
        val movie2 = Movie(MovieId(UUID.randomUUID()), "Movie 2", "Desc 2", 120, "PG")
        val movies = Map(movie1.id -> movie1, movie2.id -> movie2)
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO](movies)
          result <- algebra.findAll()
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(movie1, movie2)
        }
      }
      
      "should return empty list when no movies exist" in {
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO]()
          result <- algebra.findAll()
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "create" - {
      "should create and return new movie" in {
        val movie = Movie(MovieId(UUID.randomUUID()), "New Movie", "New Desc", 150, "R")
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO]()
          result <- algebra.create(movie)
          retrieved <- algebra.findById(movie.id)
        } yield (result, retrieved)
        
        test.asserting { case (created, retrieved) =>
          created shouldBe movie
          retrieved shouldBe Some(movie)
        }
      }
    }
    
    "update" - {
      "should update existing movie" in {
        val movieId = MovieId(UUID.randomUUID())
        val originalMovie = Movie(movieId, "Original", "Original Desc", 90, "G")
        val updatedMovie = Movie(movieId, "Updated", "Updated Desc", 120, "PG")
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO](Map(movieId -> originalMovie))
          result <- algebra.update(updatedMovie)
          retrieved <- algebra.findById(movieId)
        } yield (result, retrieved)
        
        test.asserting { case (updated, retrieved) =>
          updated shouldBe Some(updatedMovie)
          retrieved shouldBe Some(updatedMovie)
        }
      }
      
      "should return None when movie doesn't exist" in {
        val movie = Movie(MovieId(UUID.randomUUID()), "Non-existent", "Desc", 90, "G")
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO]()
          result <- algebra.update(movie)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "delete" - {
      "should delete existing movie" in {
        val movieId = MovieId(UUID.randomUUID())
        val movie = Movie(movieId, "To Delete", "Desc", 90, "G")
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO](Map(movieId -> movie))
          result <- algebra.delete(movieId)
          retrieved <- algebra.findById(movieId)
        } yield (result, retrieved)
        
        test.asserting { case (deleted, retrieved) =>
          deleted shouldBe true
          retrieved shouldBe None
        }
      }
      
      "should return false when movie doesn't exist" in {
        val movieId = MovieId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryMovieAlgebra[IO]()
          result <- algebra.delete(movieId)
        } yield result
        
        test.asserting(_ shouldBe false)
      }
    }
  }
} 