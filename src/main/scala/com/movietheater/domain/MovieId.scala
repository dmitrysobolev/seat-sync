package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.util.UUID

case class MovieId(value: UUID)

object MovieId {
  implicit val show: Show[MovieId] = Show.show(_.value.toString)
  
  implicit val encoder: Encoder[MovieId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[MovieId] = Decoder[UUID].map(MovieId.apply)
  
  implicit val get: Get[MovieId] = Get[UUID].map(MovieId.apply)
  implicit val put: Put[MovieId] = Put[UUID].contramap(_.value)
} 