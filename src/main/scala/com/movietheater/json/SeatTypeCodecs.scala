package com.movietheater.json

import com.movietheater.domain.SeatType
import io.circe.{Decoder, Encoder}

object SeatTypeCodecs {
  implicit val encoder: Encoder[SeatType] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[SeatType] = Decoder[String].emap {
    case "standard" => Right(SeatType.Standard)
    case "premium" => Right(SeatType.Premium)
    case "vip" => Right(SeatType.VIP)
    case other => Left(s"Invalid seat type: $other")
  }
} 