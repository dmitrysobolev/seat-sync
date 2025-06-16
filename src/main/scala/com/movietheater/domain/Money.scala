package com.movietheater.domain

final case class Money(cents: Long) extends AnyVal {
  def +(other: Money): Money = Money(this.cents + other.cents)
  def -(other: Money): Money = Money(this.cents - other.cents)
  def *(multiplier: Int): Money = Money(this.cents * multiplier)
  def /(divisor: Int): Money = Money(this.cents / divisor)
  
  def toDollars: (Long, Int) = (cents / 100, (cents % 100).toInt)
  
  override def toString: String = {
    val (dollars, cents) = toDollars
    f"$$$dollars.$cents%02d"
  }
}

object Money {
  def fromDollars(dollars: Long, cents: Int): Money = {
    require(cents >= 0 && cents < 100, "Cents must be between 0 and 99")
    Money(dollars * 100 + cents)
  }
  
  def fromCents(cents: Long): Money = Money(cents)
  val zero: Money = Money(0)
} 