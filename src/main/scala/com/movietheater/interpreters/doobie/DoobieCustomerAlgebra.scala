package com.movietheater.interpreters

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.CustomerAlgebra
import java.util.UUID

class DoobieCustomerAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends CustomerAlgebra[F] {

  import DoobieCustomerAlgebra._
  import DoobieInstances._

  def findById(customerId: CustomerId): F[Option[Customer]] = {
    selectByIdQuery(customerId).option.transact(xa)
  }

  def findByEmail(email: String): F[Option[Customer]] = {
    selectByEmailQuery(email).option.transact(xa)
  }

  def findAll(): F[List[Customer]] = {
    selectAllQuery.to[List].transact(xa)
  }

  def create(customer: Customer): F[Customer] = {
    insertQuery(customer).run.transact(xa).as(customer)
  }

  def update(customer: Customer): F[Option[Customer]] = {
    updateQuery(customer).run.transact(xa).map {
      case 0 => None
      case _ => Some(customer)
    }
  }

  def delete(customerId: CustomerId): F[Boolean] = {
    deleteQuery(customerId).run.transact(xa).map(_ > 0)
  }

  def deleteAll(): F[Unit] = {
    sql"DELETE FROM customers".update.run.transact(xa).void
  }
}

object DoobieCustomerAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Customer
  private implicit val customerRead: Read[Customer] = 
    Read[(UUID, String, String, String)].map {
      case (id, email, firstName, lastName) =>
        Customer(CustomerId(id), email, firstName, lastName)
    }
  
  // SQL queries
  private def selectByIdQuery(customerId: CustomerId): Query0[Customer] = {
    sql"""
      SELECT id, email, first_name, last_name 
      FROM customers 
      WHERE id = ${customerId.value}
    """.query[Customer]
  }

  private def selectByEmailQuery(email: String): Query0[Customer] = {
    sql"""
      SELECT id, email, first_name, last_name 
      FROM customers 
      WHERE email = $email
    """.query[Customer]
  }

  private val selectAllQuery: Query0[Customer] = {
    sql"""
      SELECT id, email, first_name, last_name 
      FROM customers 
      ORDER BY last_name, first_name
    """.query[Customer]
  }

  private def insertQuery(customer: Customer): Update0 = {
    sql"""
      INSERT INTO customers (id, email, first_name, last_name) 
      VALUES (${customer.id.value}, ${customer.email}, ${customer.firstName}, ${customer.lastName})
    """.update
  }

  private def updateQuery(customer: Customer): Update0 = {
    sql"""
      UPDATE customers 
      SET email = ${customer.email}, 
          first_name = ${customer.firstName}, 
          last_name = ${customer.lastName},
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${customer.id.value}
    """.update
  }

  private def deleteQuery(customerId: CustomerId): Update0 = {
    sql"DELETE FROM customers WHERE id = ${customerId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): CustomerAlgebra[F] = 
    new DoobieCustomerAlgebra[F](xa)
} 