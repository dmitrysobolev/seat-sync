package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}
import java.util.UUID

case class AuditoriumId(value: UUID)

object AuditoriumId {
  implicit val show: Show[AuditoriumId] = Show.show(_.value.toString)
  
  implicit val encoder: Encoder[AuditoriumId] = Encoder[UUID].contramap(_.value)
  implicit val decoder: Decoder[AuditoriumId] = Decoder[UUID].map(AuditoriumId.apply)
  
  implicit val get: Get[AuditoriumId] = Get[UUID].map(AuditoriumId.apply)
  implicit val put: Put[AuditoriumId] = Put[UUID].contramap(_.value)
} 