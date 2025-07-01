package com.movietheater.algebras

import cats.effect.IO
import com.movietheater.domain._
import com.movietheater.interpreters.inmemory.InMemoryCustomerAlgebra
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global
import java.util.UUID

class CustomerAlgebraSuite extends AnyFunSuite with Matchers {
  test("CustomerAlgebra - create and findById") {
    val customer = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO]()
      created <- algebra.create(customer)
      retrieved <- algebra.findById(customer.id)
    } yield (created, retrieved)

    val (created, retrieved) = program.unsafeRunSync()
    created should be(customer)
    retrieved should be(Some(customer))
  }

  test("CustomerAlgebra - findByEmail") {
    val customer1 = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")
    val customer2 = Customer(CustomerId(UUID.randomUUID()), "jane.smith@example.com", "Jane", "Smith")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer1.id -> customer1, customer2.id -> customer2))
      retrieved <- algebra.findByEmail("jane.smith@example.com")
    } yield retrieved

    program.unsafeRunSync() should be(Some(customer2))
  }

  test("CustomerAlgebra - findByEmail not found") {
    val customer = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer.id -> customer))
      retrieved <- algebra.findByEmail("nonexistent@example.com")
    } yield retrieved

    program.unsafeRunSync() should be(None)
  }

  test("CustomerAlgebra - findAll") {
    val customer1 = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")
    val customer2 = Customer(CustomerId(UUID.randomUUID()), "jane.smith@example.com", "Jane", "Smith")
    val customer3 = Customer(CustomerId(UUID.randomUUID()), "bob.wilson@example.com", "Bob", "Wilson")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer1.id -> customer1, customer2.id -> customer2, customer3.id -> customer3))
      retrieved <- algebra.findAll()
    } yield retrieved

    val result = program.unsafeRunSync()
    result should have size 3
    result should contain allOf (customer1, customer2, customer3)
  }

  test("CustomerAlgebra - update") {
    val customer = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")
    val updatedCustomer = customer.copy(firstName = "Johnny", lastName = "Smith")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer.id -> customer))
      updated <- algebra.update(updatedCustomer)
      retrieved <- algebra.findById(customer.id)
    } yield (updated, retrieved)

    val (updated, retrieved) = program.unsafeRunSync()
    updated should be(Some(updatedCustomer))
    retrieved should be(Some(updatedCustomer))
  }

  test("CustomerAlgebra - update non-existent customer") {
    val customer = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")
    val nonExistentCustomer = Customer(CustomerId(UUID.randomUUID()), "jane.doe@example.com", "Jane", "Doe")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer.id -> customer))
      updated <- algebra.update(nonExistentCustomer)
    } yield updated

    program.unsafeRunSync() should be(None)
  }

  test("CustomerAlgebra - delete") {
    val customer = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer.id -> customer))
      deleted <- algebra.delete(customer.id)
      retrieved <- algebra.findById(customer.id)
    } yield (deleted, retrieved)

    val (deleted, retrieved) = program.unsafeRunSync()
    deleted should be(true)
    retrieved should be(None)
  }

  test("CustomerAlgebra - delete non-existent customer") {
    val customer = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")
    val nonExistentId = CustomerId(UUID.randomUUID())

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer.id -> customer))
      deleted <- algebra.delete(nonExistentId)
      retrieved <- algebra.findById(customer.id)
    } yield (deleted, retrieved)

    val (deleted, retrieved) = program.unsafeRunSync()
    deleted should be(false)
    retrieved should be(Some(customer)) // Original customer should still exist
  }

  test("CustomerAlgebra - deleteAll") {
    val customer1 = Customer(CustomerId(UUID.randomUUID()), "john.doe@example.com", "John", "Doe")
    val customer2 = Customer(CustomerId(UUID.randomUUID()), "jane.smith@example.com", "Jane", "Smith")

    val program = for {
      algebra <- InMemoryCustomerAlgebra[IO](Map(customer1.id -> customer1, customer2.id -> customer2))
      _ <- algebra.deleteAll()
      retrieved <- algebra.findAll()
    } yield retrieved

    program.unsafeRunSync() should be(List.empty)
  }
}
