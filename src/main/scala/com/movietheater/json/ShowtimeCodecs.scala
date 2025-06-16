package com.movietheater.json

import com.movietheater.domain.{Showtime, ShowtimeId, MovieId, TheaterId, AuditoriumId, SeatId, SeatType, Money, SeatStatus}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime

object ShowtimeCodecs {
  implicit val encoder: Encoder[Showtime] = Encoder.forProduct10(
    "id", "movieId", "theaterId", "auditoriumId", "startTime", "seatTypes", "seatPrices", "seatStatus", "createdAt", "updatedAt"
  )(showtime => (
    showtime.id,
    showtime.movieId,
    showtime.theaterId,
    showtime.auditoriumId,
    showtime.startTime,
    showtime.seatTypes,
    showtime.seatPrices,
    showtime.seatStatus,
    showtime.createdAt,
    showtime.updatedAt
  ))
  
  implicit val decoder: Decoder[Showtime] = Decoder.forProduct10(
    "id", "movieId", "theateriumId", "auditoriumId", "startTime", "seatTypes", "seatPrices", "seatStatus", "createdAt", "updatedAt"
  )(Showtime.apply)
} 