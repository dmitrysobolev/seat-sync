package com.movietheater.json

import com.movietheater.domain.{Showtime, ShowtimeId, MovieId, TheaterId, AuditoriumId, SeatId, SeatType, Money, SeatStatus}
import ShowtimeIdCodecs._
import MovieIdCodecs._
import TheaterIdCodecs._
import AuditoriumIdCodecs._
import SeatIdCodecs._
import MoneyCodecs._
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import java.time.LocalDateTime

object ShowtimeCodecs {
  implicit val encoder: Encoder[Showtime] = deriveEncoder[Showtime]
  implicit val decoder: Decoder[Showtime] = deriveDecoder[Showtime]
} 
