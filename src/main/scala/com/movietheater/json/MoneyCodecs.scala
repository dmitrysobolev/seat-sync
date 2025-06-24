package com.movietheater.json

import com.movietheater.domain.Money
import io.circe.{Decoder, Encoder}

object MoneyCodecs {
  implicit val encoder: Encoder[Money] = Encoder[Long].contramap(_.cents)
  implicit val decoder: Decoder[Money] = Decoder[Long].map(Money.apply)
}