package com.movietheater.json

import com.movietheater.domain.SeatId
import io.circe.{Decoder, Encoder}

object SeatIdCodecs {
  implicit val encoder: Encoder[SeatId] = Encoder[String].contramap(_.value)
  implicit val decoder: Decoder[SeatId] = Decoder[String].map(SeatId.apply)
}