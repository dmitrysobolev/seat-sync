package com.movietheater.interpreters.doobie

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.MovieAlgebra
import java.util.UUID

class DoobieMovieAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends MovieAlgebra[F] {

  import DoobieMovieAlgebra._

  def findById(movieId: MovieId): F[Option[Movie]] = {
    selectByIdQuery(movieId).option.transact(xa)
  }

  def findAll(): F[List[Movie]] = {
    selectAllQuery.to[List].transact(xa)
  }

  def create(movie: Movie): F[Movie] = {
    insertQuery(movie).run.transact(xa).as(movie)
  }

  def update(movie: Movie): F[Option[Movie]] = {
    updateQuery(movie).run.transact(xa).map {
      case 0 => None
      case _ => Some(movie)
    }
  }

  def delete(movieId: MovieId): F[Boolean] = {
    deleteQuery(movieId).run.transact(xa).map(_ > 0)
  }

  def deleteAll(): F[Unit] = {
    sql"DELETE FROM movies".update.run.transact(xa).void
  }
}

object DoobieMovieAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Movie
  private implicit val movieRead: Read[Movie] = Read[(UUID, String, String, Int, String)].map {
    case (id, title, description, duration, rating) =>
      Movie(MovieId(id), title, description, duration, rating)
  }
  
  // SQL queries
  private def selectByIdQuery(movieId: MovieId): Query0[Movie] = {
    sql"""
      SELECT id, title, description, duration_minutes, rating 
      FROM movies 
      WHERE id = ${movieId.value}
    """.query[Movie]
  }

  private val selectAllQuery: Query0[Movie] = {
    sql"""
      SELECT id, title, description, duration_minutes, rating 
      FROM movies 
      ORDER BY title
    """.query[Movie]
  }

  private def insertQuery(movie: Movie): Update0 = {
    sql"""
      INSERT INTO movies (id, title, description, duration_minutes, rating) 
      VALUES (${movie.id.value}, ${movie.title}, ${movie.description}, ${movie.durationMinutes}, ${movie.rating})
    """.update
  }

  private def updateQuery(movie: Movie): Update0 = {
    sql"""
      UPDATE movies 
      SET title = ${movie.title}, 
          description = ${movie.description}, 
          duration_minutes = ${movie.durationMinutes}, 
          rating = ${movie.rating},
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${movie.id.value}
    """.update
  }

  private def deleteQuery(movieId: MovieId): Update0 = {
    sql"DELETE FROM movies WHERE id = ${movieId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): MovieAlgebra[F] = 
    new DoobieMovieAlgebra[F](xa)
} 