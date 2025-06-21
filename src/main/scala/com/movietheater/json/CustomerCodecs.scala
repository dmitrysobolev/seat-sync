package com.movietheater.json

import com.movietheater.domain.{Customer, CustomerId}
import io.circe.{Decoder, Encoder}

object CustomerCodecs {
  implicit val encoder: Encoder[Customer] = Encoder.forProduct4(
    "id", "email", "firstName", "lastName"
  )(customer => (
    customer.id,
    customer.email,
    customer.firstName,
    customer.lastName
  ))
  
  implicit val decoder: Decoder[Customer] = Decoder.forProduct4(
    "id", "email", "firstName", "lastName"
  )(Customer.apply)
} 