package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}

sealed trait SeatStatus {
  def toString: String
}

object SeatStatus {
  case object Available extends SeatStatus {
    override def toString: String = "available"
  }
  
  case object Reserved extends SeatStatus {
    override def toString: String = "reserved"
  }
  
  case object Sold extends SeatStatus {
    override def toString: String = "sold"
  }
  
  implicit val show: Show[SeatStatus] = Show.show(_.toString)
  
  implicit val encoder: Encoder[SeatStatus] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[SeatStatus] = Decoder[String].emap {
    case "available" => Right(Available)
    case "reserved" => Right(Reserved)
    case "sold" => Right(Sold)
    case other => Left(s"Invalid seat status: $other")
  }
  
  implicit val get: Get[SeatStatus] = Get[String].map {
    case "available" => Available
    case "reserved" => Reserved
    case "sold" => Sold
    case other => throw new IllegalArgumentException(s"Invalid seat status: $other")
  }
  
  implicit val put: Put[SeatStatus] = Put[String].contramap(_.toString)
  
  def fromString(str: String): Option[SeatStatus] = str match {
    case "available" => Some(Available)
    case "reserved" => Some(Reserved)
    case "sold" => Some(Sold)
    case _ => None
  }
} 