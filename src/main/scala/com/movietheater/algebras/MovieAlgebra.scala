package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._

trait MovieAlgebra[F[_]] {
  def findById(movieId: MovieId): F[Option[Movie]]
  def findAll(): F[List[Movie]]
  def create(movie: Movie): F[Movie]
  def update(movie: Movie): F[Option[Movie]]
  def delete(movieId: MovieId): F[Boolean]
  def deleteAll(): F[Unit]
}

object MovieAlgebra {
  def apply[F[_]](implicit ev: MovieAlgebra[F]): MovieAlgebra[F] = ev
} 