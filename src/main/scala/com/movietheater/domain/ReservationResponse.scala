package com.movietheater.domain

case class ReservationResponse(
  tickets: List[Ticket],
  totalPrice: Money
)