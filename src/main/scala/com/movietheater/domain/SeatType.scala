package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}

sealed trait SeatType {
  def toString: String
}

object SeatType {
  case object Standard extends SeatType {
    override def toString: String = "standard"
  }
  
  case object Premium extends SeatType {
    override def toString: String = "premium"
  }
  
  case object VIP extends SeatType {
    override def toString: String = "vip"
  }
  
  implicit val show: Show[SeatType] = Show.show(_.toString)
  
  implicit val encoder: Encoder[SeatType] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[SeatType] = Decoder[String].emap {
    case "standard" => Right(Standard)
    case "premium" => Right(Premium)
    case "vip" => Right(VIP)
    case other => Left(s"Invalid seat type: $other")
  }
  
  implicit val get: Get[SeatType] = Get[String].map {
    case "standard" => Standard
    case "premium" => Premium
    case "vip" => VIP
    case other => throw new IllegalArgumentException(s"Invalid seat type: $other")
  }
  
  implicit val put: Put[SeatType] = Put[String].contramap(_.toString)
  
  def fromString(str: String): Option[SeatType] = str match {
    case "standard" => Some(Standard)
    case "premium" => Some(Premium)
    case "vip" => Some(VIP)
    case _ => None
  }
} 