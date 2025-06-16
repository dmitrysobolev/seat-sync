package com.movietheater.domain

case class CreateReservationRequest(
  showtimeId: ShowtimeId,
  customerId: CustomerId,
  seatIds: List[SeatId]
)