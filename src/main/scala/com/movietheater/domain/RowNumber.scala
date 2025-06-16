package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}

case class RowNumber(value: Char) {
  override def toString: String = value.toString
}

object RowNumber {
  implicit val show: Show[RowNumber] = Show.show(_.toString)
  
  implicit val encoder: Encoder[RowNumber] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[RowNumber] = Decoder[String].emap { str =>
    if (str.length == 1 && str.head.isLetter) {
      Right(RowNumber(str.head.toUpper))
    } else {
      Left("Row number must be a single letter")
    }
  }
  
  implicit val get: Get[RowNumber] = Get[String].map(str => RowNumber(str.head.toUpper))
  implicit val put: Put[RowNumber] = Put[String].contramap(_.toString)
  
  def fromString(str: String): Option[RowNumber] = {
    if (str.length == 1 && str.head.isLetter) {
      Some(RowNumber(str.head.toUpper))
    } else {
      None
    }
  }
} 