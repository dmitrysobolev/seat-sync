package com.movietheater.json

import com.movietheater.domain.ShowtimeId
import io.circe.{Decoder, Encoder}
import java.util.UUID

object ShowtimeIdCodecs {
  implicit val encoder: Encoder[ShowtimeId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[ShowtimeId] = Decoder[UUID].map(ShowtimeId.apply)
} 