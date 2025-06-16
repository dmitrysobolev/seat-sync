package com.movietheater.json

import com.movietheater.domain.MovieId
import io.circe.{Decoder, Encoder}
import java.util.UUID

object MovieIdCodecs {
  implicit val encoder: Encoder[MovieId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[MovieId] = Decoder[UUID].map(MovieId.apply)
} 