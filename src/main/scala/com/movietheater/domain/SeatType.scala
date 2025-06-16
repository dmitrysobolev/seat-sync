package com.movietheater.domain

import cats.Show
import cats.implicits._

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
  
  def fromString(str: String): Option[SeatType] = str match {
    case "standard" => Some(Standard)
    case "premium" => Some(Premium)
    case "vip" => Some(VIP)
    case _ => None
  }
} 