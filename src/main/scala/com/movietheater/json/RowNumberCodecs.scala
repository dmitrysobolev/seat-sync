package com.movietheater.json

import com.movietheater.domain.RowNumber
import io.circe.{Decoder, Encoder}

object RowNumberCodecs {
  implicit val encoder: Encoder[RowNumber] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[RowNumber] = Decoder[String].emap { str =>
    if (str.length == 1 && str.head.isLetter) {
      Right(RowNumber(str.head.toUpper))
    } else {
      Left("Row number must be a single letter")
    }
  }
} 