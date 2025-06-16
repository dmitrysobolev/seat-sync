package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.util.UUID

case class ShowtimeId(value: UUID)

object ShowtimeId {
  implicit val show: Show[ShowtimeId] = Show.show(_.value.toString)
  
  implicit val encoder: Encoder[ShowtimeId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[ShowtimeId] = Decoder[UUID].map(ShowtimeId.apply)
  
  implicit val get: Get[ShowtimeId] = Get[UUID].map(ShowtimeId.apply)
  implicit val put: Put[ShowtimeId] = Put[UUID].contramap(_.value)
} 