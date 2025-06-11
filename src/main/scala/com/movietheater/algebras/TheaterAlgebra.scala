package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._

trait TheaterAlgebra[F[_]] {
  def findById(theaterId: TheaterId): F[Option[Theater]]
  def findAll(): F[List[Theater]]
  def create(theater: Theater): F[Theater]
  def update(theater: Theater): F[Option[Theater]]
  def delete(theaterId: TheaterId): F[Boolean]
}

object TheaterAlgebra {
  def apply[F[_]](implicit ev: TheaterAlgebra[F]): TheaterAlgebra[F] = ev
} 