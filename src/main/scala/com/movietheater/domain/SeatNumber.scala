package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}

case class SeatNumber(value: Int) {
  override def toString: String = value.toString
}

object SeatNumber {
  implicit val show: Show[SeatNumber] = Show.show(_.toString)
  
  implicit val encoder: Encoder[SeatNumber] = Encoder[Int].contramap(_.value)
  implicit val decoder: Decoder[SeatNumber] = Decoder[Int].map(SeatNumber.apply)
  
  implicit val get: Get[SeatNumber] = Get[Int].map(SeatNumber.apply)
  implicit val put: Put[SeatNumber] = Put[Int].contramap(_.value)
} 