package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

case class Customer(
  id: CustomerId,
  name: String,
  email: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Customer {
  implicit val show: Show[Customer] = Show.show(customer => 
    s"Customer ${customer.name} (${customer.email})"
  )
  
  implicit val encoder: Encoder[Customer] = Encoder.forProduct5(
    "id", "name", "email", "createdAt", "updatedAt"
  )(customer => (
    customer.id,
    customer.name,
    customer.email,
    customer.createdAt,
    customer.updatedAt
  ))
  
  implicit val decoder: Decoder[Customer] = Decoder.forProduct5(
    "id", "name", "email", "createdAt", "updatedAt"
  )(Customer.apply)
  
  implicit val get: Get[Customer] = Get[(CustomerId, String, String, LocalDateTime, LocalDateTime)].map {
    case (id, name, email, createdAt, updatedAt) =>
      Customer(id, name, email, createdAt, updatedAt)
  }
  
  implicit val put: Put[Customer] = Put[(CustomerId, String, String, LocalDateTime, LocalDateTime)].contramap { customer =>
    (
      customer.id,
      customer.name,
      customer.email,
      customer.createdAt,
      customer.updatedAt
    )
  }
} 