package com.movietheater.json

import com.movietheater.domain.CustomerId
import io.circe.{Decoder, Encoder}
import java.util.UUID

object CustomerIdCodecs {
  implicit val encoder: Encoder[CustomerId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[CustomerId] = Decoder[UUID].map(CustomerId.apply)
} 