package com.movietheater.domain

import cats.Show
import cats.implicits._

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
  
  def fromString(str: String): Option[SeatStatus] = str match {
    case "available" => Some(Available)
    case "reserved" => Some(Reserved)
    case "sold" => Some(Sold)
    case _ => None
  }
} 