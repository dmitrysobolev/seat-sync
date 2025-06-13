package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._

trait CustomerAlgebra[F[_]] {
  def findById(customerId: CustomerId): F[Option[Customer]]
  def findByEmail(email: String): F[Option[Customer]]
  def findAll(): F[List[Customer]]
  def create(customer: Customer): F[Customer]
  def update(customer: Customer): F[Option[Customer]]
  def delete(customerId: CustomerId): F[Boolean]
  def deleteAll(): F[Unit]
}

object CustomerAlgebra {
  def apply[F[_]](implicit ev: CustomerAlgebra[F]): CustomerAlgebra[F] = ev
} 