package com.movietheater.interpreters.inmemory

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.MovieAlgebra

class InMemoryMovieAlgebra[F[_]: Sync](ref: Ref[F, Map[MovieId, Movie]]) extends MovieAlgebra[F] {

  def findById(movieId: MovieId): F[Option[Movie]] = {
    ref.get.map(_.get(movieId))
  }

  def findAll(): F[List[Movie]] = {
    ref.get.map(_.values.toList)
  }

  def create(movie: Movie): F[Movie] = {
    ref.modify { movies =>
      val updated = movies + (movie.id -> movie)
      (updated, movie)
    }
  }

  def update(movie: Movie): F[Option[Movie]] = {
    ref.modify { movies =>
      movies.get(movie.id) match {
        case Some(_) =>
          val updated = movies + (movie.id -> movie)
          (updated, Some(movie))
        case None =>
          (movies, None)
      }
    }
  }

  def delete(movieId: MovieId): F[Boolean] = {
    ref.modify { movies =>
      movies.get(movieId) match {
        case Some(_) =>
          val updated = movies - movieId
          (updated, true)
        case None =>
          (movies, false)
      }
    }
  }

  def deleteAll(): F[Unit] = ref.set(Map.empty)
}

object InMemoryMovieAlgebra {
  def apply[F[_]: Sync](initialData: Map[MovieId, Movie] = Map.empty): F[MovieAlgebra[F]] = {
    Ref.of[F, Map[MovieId, Movie]](initialData).map(new InMemoryMovieAlgebra[F](_))
  }
} 