package com.movietheater.interpreters

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec
import com.movietheater.domain._
import java.util.UUID

class InMemoryCustomerAlgebraSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "InMemoryCustomerAlgebra" - {
    "findById" - {
      "should return customer when it exists" in {
        val customerId = CustomerId(UUID.randomUUID())
        val customer = Customer(customerId, "test@example.com", "John", "Doe")
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> customer))
          result <- algebra.findById(customerId)
        } yield result
        
        test.asserting(_ shouldBe Some(customer))
      }
      
      "should return None when customer doesn't exist" in {
        val customerId = CustomerId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO]()
          result <- algebra.findById(customerId)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findByEmail" - {
      "should return customer when email exists" in {
        val customer = Customer(CustomerId(UUID.randomUUID()), "test@example.com", "John", "Doe")
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO](Map(customer.id -> customer))
          result <- algebra.findByEmail("test@example.com")
        } yield result
        
        test.asserting(_ shouldBe Some(customer))
      }
      
      "should return None when email doesn't exist" in {
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO]()
          result <- algebra.findByEmail("nonexistent@example.com")
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "findAll" - {
      "should return all customers" in {
        val customer1 = Customer(CustomerId(UUID.randomUUID()), "john@example.com", "John", "Doe")
        val customer2 = Customer(CustomerId(UUID.randomUUID()), "jane@example.com", "Jane", "Smith")
        val customers = Map(customer1.id -> customer1, customer2.id -> customer2)
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO](customers)
          result <- algebra.findAll()
        } yield result
        
        test.asserting { result =>
          result should contain theSameElementsAs List(customer1, customer2)
        }
      }
      
      "should return empty list when no customers exist" in {
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO]()
          result <- algebra.findAll()
        } yield result
        
        test.asserting(_ shouldBe List.empty)
      }
    }
    
    "create" - {
      "should create and return new customer" in {
        val customer = Customer(CustomerId(UUID.randomUUID()), "new@example.com", "New", "Customer")
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO]()
          result <- algebra.create(customer)
          retrieved <- algebra.findById(customer.id)
        } yield (result, retrieved)
        
        test.asserting { case (created, retrieved) =>
          created shouldBe customer
          retrieved shouldBe Some(customer)
        }
      }
    }
    
    "update" - {
      "should update existing customer" in {
        val customerId = CustomerId(UUID.randomUUID())
        val originalCustomer = Customer(customerId, "original@example.com", "Original", "Name")
        val updatedCustomer = Customer(customerId, "updated@example.com", "Updated", "Name")
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> originalCustomer))
          result <- algebra.update(updatedCustomer)
          retrieved <- algebra.findById(customerId)
        } yield (result, retrieved)
        
        test.asserting { case (updated, retrieved) =>
          updated shouldBe Some(updatedCustomer)
          retrieved shouldBe Some(updatedCustomer)
        }
      }
      
      "should return None when customer doesn't exist" in {
        val customer = Customer(CustomerId(UUID.randomUUID()), "nonexistent@example.com", "Non", "Existent")
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO]()
          result <- algebra.update(customer)
        } yield result
        
        test.asserting(_ shouldBe None)
      }
    }
    
    "delete" - {
      "should delete existing customer" in {
        val customerId = CustomerId(UUID.randomUUID())
        val customer = Customer(customerId, "delete@example.com", "To", "Delete")
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO](Map(customerId -> customer))
          result <- algebra.delete(customerId)
          retrieved <- algebra.findById(customerId)
        } yield (result, retrieved)
        
        test.asserting { case (deleted, retrieved) =>
          deleted shouldBe true
          retrieved shouldBe None
        }
      }
      
      "should return false when customer doesn't exist" in {
        val customerId = CustomerId(UUID.randomUUID())
        
        val test = for {
          algebra <- InMemoryCustomerAlgebra[IO]()
          result <- algebra.delete(customerId)
        } yield result
        
        test.asserting(_ shouldBe false)
      }
    }
  }
} 