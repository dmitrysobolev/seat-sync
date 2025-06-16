package com.movietheater.json

import com.movietheater.domain.TheaterId
import io.circe.{Decoder, Encoder}
import java.util.UUID

object TheaterIdCodecs {
  implicit val encoder: Encoder[TheaterId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[TheaterId] = Decoder[UUID].map(TheaterId.apply)
} 