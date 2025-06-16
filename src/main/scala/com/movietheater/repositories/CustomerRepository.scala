package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Customer, CustomerId}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}
import java.time.LocalDateTime

class CustomerRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Customer] = (
    Get[CustomerId].map(_.asInstanceOf[CustomerId]) <*>
      Get[String] <*>
      Get[String] <*>
      Get[String] <*>
      Get[LocalDateTime] <*>
      Get[LocalDateTime]
    ).map {
    case (id, email, firstName, lastName, createdAt, updatedAt) =>
      Customer(id, email, firstName, lastName, createdAt, updatedAt)
  }

  implicit val put: Put[Customer] = (
    Put[CustomerId] <*>
      Put[String] <*>
      Put[String] <*>
      Put[String] <*>
      Put[LocalDateTime] <*>
      Put[LocalDateTime]
    ).contramap { customer =>
    (
      customer.id,
      customer.email,
      customer.firstName,
      customer.lastName,
      customer.createdAt,
      customer.updatedAt
    )
  }

  def findById(customerId: CustomerId): IO[Option[Customer]] = {
    sql"""
      SELECT id, email, first_name, last_name, created_at, updated_at
      FROM customers
      WHERE id = $customerId
    """.query[Customer].option.transact(xa)
  }

  def findAll: IO[List[Customer]] = {
    sql"""
      SELECT id, email, first_name, last_name, created_at, updated_at
      FROM customers
    """.query[Customer].stream.compile.toList.transact(xa)
  }

  def create(customer: Customer): IO[Customer] = {
    sql"""
      INSERT INTO customers (id, email, first_name, last_name, created_at, updated_at)
      VALUES (${customer.id}, ${customer.email}, ${customer.firstName}, ${customer.lastName}, 
              ${customer.createdAt}, ${customer.updatedAt})
    """.update.run.transact(xa).map(_ => customer)
  }

  def update(customer: Customer): IO[Customer] = {
    sql"""
      UPDATE customers
      SET email = ${customer.email},
          first_name = ${customer.firstName},
          last_name = ${customer.lastName},
          updated_at = ${customer.updatedAt}
      WHERE id = ${customer.id}
    """.update.run.transact(xa).map(_ => customer)
  }

  def delete(customerId: CustomerId): IO[Unit] = {
    sql"""
      DELETE FROM customers
      WHERE id = $customerId
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM customers
    """.update.run.transact(xa).void
  }
} 