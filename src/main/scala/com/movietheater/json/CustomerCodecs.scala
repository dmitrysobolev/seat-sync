package com.movietheater.json

import com.movietheater.domain.{Customer, CustomerId}
import CustomerIdCodecs._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object CustomerCodecs {
  implicit val encoder: Encoder[Customer] = deriveEncoder[Customer]
  implicit val decoder: Decoder[Customer] = deriveDecoder[Customer]
} 
