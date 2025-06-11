package com.movietheater.interpreters

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.CustomerAlgebra

class InMemoryCustomerAlgebra[F[_]: Sync](ref: Ref[F, Map[CustomerId, Customer]]) extends CustomerAlgebra[F] {

  def findById(customerId: CustomerId): F[Option[Customer]] = {
    ref.get.map(_.get(customerId))
  }

  def findByEmail(email: String): F[Option[Customer]] = {
    ref.get.map(_.values.find(_.email == email))
  }

  def findAll(): F[List[Customer]] = {
    ref.get.map(_.values.toList)
  }

  def create(customer: Customer): F[Customer] = {
    ref.modify { customers =>
      val updated = customers + (customer.id -> customer)
      (updated, customer)
    }
  }

  def update(customer: Customer): F[Option[Customer]] = {
    ref.modify { customers =>
      customers.get(customer.id) match {
        case Some(_) =>
          val updated = customers + (customer.id -> customer)
          (updated, Some(customer))
        case None =>
          (customers, None)
      }
    }
  }

  def delete(customerId: CustomerId): F[Boolean] = {
    ref.modify { customers =>
      customers.get(customerId) match {
        case Some(_) =>
          val updated = customers - customerId
          (updated, true)
        case None =>
          (customers, false)
      }
    }
  }
}

object InMemoryCustomerAlgebra {
  def apply[F[_]: Sync](initialData: Map[CustomerId, Customer] = Map.empty): F[CustomerAlgebra[F]] = {
    Ref.of[F, Map[CustomerId, Customer]](initialData).map(new InMemoryCustomerAlgebra[F](_))
  }
} 