package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.util.UUID

case class CustomerId(value: UUID)

object CustomerId {
  implicit val show: Show[CustomerId] = Show.show(_.value.toString)
  
  implicit val encoder: Encoder[CustomerId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[CustomerId] = Decoder[UUID].map(CustomerId.apply)
  
  implicit val get: Get[CustomerId] = Get[UUID].map(CustomerId.apply)
  implicit val put: Put[CustomerId] = Put[UUID].contramap(_.value)
} 