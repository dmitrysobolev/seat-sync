package com.movietheater.domain

import cats.Show
import cats.implicits._
import doobie.util.{Get, Put}
import io.circe.{Decoder, Encoder}

sealed trait TicketStatus {
  def toString: String
}

object TicketStatus {
  case object Reserved extends TicketStatus {
    override def toString: String = "reserved"
  }
  
  case object Paid extends TicketStatus {
    override def toString: String = "paid"
  }
  
  case object Cancelled extends TicketStatus {
    override def toString: String = "cancelled"
  }
  
  implicit val show: Show[TicketStatus] = Show.show(_.toString)
  
  implicit val encoder: Encoder[TicketStatus] = Encoder[String].contramap(_.toString)
  implicit val decoder: Decoder[TicketStatus] = Decoder[String].emap {
    case "reserved" => Right(Reserved)
    case "paid" => Right(Paid)
    case "cancelled" => Right(Cancelled)
    case other => Left(s"Invalid ticket status: $other")
  }
  
  implicit val get: Get[TicketStatus] = Get[String].map {
    case "reserved" => Reserved
    case "paid" => Paid
    case "cancelled" => Cancelled
    case other => throw new IllegalArgumentException(s"Invalid ticket status: $other")
  }
  
  implicit val put: Put[TicketStatus] = Put[String].contramap(_.toString)
  
  def fromString(str: String): Option[TicketStatus] = str match {
    case "reserved" => Some(Reserved)
    case "paid" => Some(Paid)
    case "cancelled" => Some(Cancelled)
    case _ => None
  }
} 