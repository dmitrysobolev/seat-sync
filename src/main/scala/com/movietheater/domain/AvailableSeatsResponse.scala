package com.movietheater.domain

case class AvailableSeatsResponse(
  showtimeId: ShowtimeId,
  seats: List[Seat]
)