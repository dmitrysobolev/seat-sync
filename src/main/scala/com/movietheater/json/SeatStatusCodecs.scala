package com.movietheater.json

import com.movietheater.domain.SeatStatus
import io.circe.{Decoder, Encoder}

object SeatStatusCodecs {
  implicit val encoder: Encoder[SeatStatus] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[SeatStatus] = Decoder[String].emap {
    case "available" => Right(SeatStatus.Available)
    case "reserved" => Right(SeatStatus.Reserved)
    case "sold" => Right(SeatStatus.Sold)
    case other => Left(s"Invalid seat status: $other")
  }
} 