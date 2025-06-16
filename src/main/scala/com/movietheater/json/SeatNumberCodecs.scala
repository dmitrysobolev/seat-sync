package com.movietheater.json

import com.movietheater.domain.SeatNumber
import io.circe.{Decoder, Encoder}

object SeatNumberCodecs {
  implicit val encoder: Encoder[SeatNumber] = Encoder[Int].contramap(_.value)
  implicit val decoder: Decoder[SeatNumber] = Decoder[Int].map(SeatNumber.apply)
} 