package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Customer, CustomerId}
import com.movietheater.db.DoobieInstances._

class CustomerRepository(xa: doobie.Transactor[IO]) {
  def findById(customerId: CustomerId): IO[Option[Customer]] = {
    sql"""
      SELECT id, email, first_name, last_name
      FROM customers
      WHERE id = $customerId
    """.query[Customer].option.transact(xa)
  }

  def findAll: IO[List[Customer]] = {
    sql"""
      SELECT id, email, first_name, last_name
      FROM customers
    """.query[Customer].stream.compile.toList.transact(xa)
  }

  def create(customer: Customer): IO[Customer] = {
    sql"""
      INSERT INTO customers (id, email, first_name, last_name)
      VALUES (${customer.id}, ${customer.email}, ${customer.firstName}, ${customer.lastName})
    """.update.run.transact(xa).map(_ => customer)
  }

  def update(customer: Customer): IO[Customer] = {
    sql"""
      UPDATE customers
      SET email = ${customer.email},
          first_name = ${customer.firstName},
          last_name = ${customer.lastName}
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