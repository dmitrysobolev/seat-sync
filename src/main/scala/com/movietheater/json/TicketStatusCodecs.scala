package com.movietheater.json

import com.movietheater.domain.TicketStatus
import io.circe.{Decoder, Encoder}

object TicketStatusCodecs {
  implicit val encoder: Encoder[TicketStatus] = Encoder[String].contramap(_.toString)
  
  implicit val decoder: Decoder[TicketStatus] = Decoder[String].emap {
    case "reserved" => Right(TicketStatus.Reserved)
    case "purchased" => Right(TicketStatus.Purchased)
    case "cancelled" => Right(TicketStatus.Cancelled)
    case other => Left(s"Invalid ticket status: $other")
  }
} 