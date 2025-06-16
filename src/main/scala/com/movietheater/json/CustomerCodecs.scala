package com.movietheater.json

import com.movietheater.domain.Customer
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

object CustomerCodecs {
  implicit val encoder: Encoder[Customer] = Encoder.forProduct6(
    "id", "email", "firstName", "lastName", "createdAt", "updatedAt"
  )(customer => (
    customer.id,
    customer.email,
    customer.firstName,
    customer.lastName,
    customer.createdAt,
    customer.updatedAt
  ))
  
  implicit val decoder: Decoder[Customer] = Decoder.forProduct6(
    "id", "email", "firstName", "lastName", "createdAt", "updatedAt"
  )(Customer.apply)
} 