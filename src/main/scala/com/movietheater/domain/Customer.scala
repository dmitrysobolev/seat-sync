package com.movietheater.domain

import cats.Show
import cats.implicits._
import java.time.LocalDateTime

case class Customer(
  id: CustomerId,
  email: String,
  firstName: String,
  lastName: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Customer {
  implicit val show: Show[Customer] = Show.show(customer => 
    s"Customer ${customer.firstName} ${customer.lastName} (${customer.email})"
  )
} 