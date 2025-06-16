package com.movietheater.domain

import cats.Show
import cats.implicits._

case class RowNumber(value: Char) {
  override def toString: String = value.toString
}

object RowNumber {
  implicit val show: Show[RowNumber] = Show.show(_.toString)
  
  def fromString(str: String): Option[RowNumber] = {
    if (str.length == 1 && str.head.isLetter) {
      Some(RowNumber(str.head.toUpper))
    } else {
      None
    }
  }
} 