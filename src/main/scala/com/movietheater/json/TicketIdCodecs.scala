package com.movietheater.json

import com.movietheater.domain.TicketId
import io.circe.{Decoder, Encoder}
import java.util.UUID

object TicketIdCodecs {
  implicit val encoder: Encoder[TicketId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[TicketId] = Decoder[UUID].map(TicketId.apply)
} 