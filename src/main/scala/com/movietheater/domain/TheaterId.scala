package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.util.UUID

case class TheaterId(value: UUID)

object TheaterId {
  implicit val show: Show[TheaterId] = Show.show(_.value.toString)
  
  implicit val encoder: Encoder[TheaterId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[TheaterId] = Decoder[UUID].map(TheaterId.apply)
  
  implicit val get: Get[TheaterId] = Get[UUID].map(TheaterId.apply)
  implicit val put: Put[TheaterId] = Put[UUID].contramap(_.value)
} 