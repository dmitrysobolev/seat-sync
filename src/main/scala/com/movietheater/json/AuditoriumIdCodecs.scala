package com.movietheater.json

import com.movietheater.domain.AuditoriumId
import io.circe.{Decoder, Encoder}
import java.util.UUID

object AuditoriumIdCodecs {
  implicit val encoder: Encoder[AuditoriumId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[AuditoriumId] = Decoder[UUID].map(AuditoriumId.apply)
} 