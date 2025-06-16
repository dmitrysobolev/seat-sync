package com.movietheater.domain

import cats.Show
import cats.implicits._

sealed trait TicketStatus {
  def toString: String
}

object TicketStatus {
  case object Reserved extends TicketStatus {
    override def toString: String = "reserved"
  }
  
  case object Purchased extends TicketStatus {
    override def toString: String = "purchased"
  }
  
  case object Cancelled extends TicketStatus {
    override def toString: String = "cancelled"
  }
  
  implicit val show: Show[TicketStatus] = Show.show(_.toString)
  
  def fromString(str: String): Option[TicketStatus] = str match {
    case "reserved" => Some(Reserved)
    case "purchased" => Some(Purchased)
    case "cancelled" => Some(Cancelled)
    case _ => None
  }
} 