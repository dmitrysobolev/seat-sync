package com.movietheater.interpreters.inmemory

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.ShowtimeAlgebra
import java.time.LocalDateTime

class InMemoryShowtimeAlgebra[F[_]: Sync](ref: Ref[F, Map[ShowtimeId, Showtime]]) extends ShowtimeAlgebra[F] {

  def findById(showtimeId: ShowtimeId): F[Option[Showtime]] = {
    ref.get.map(_.get(showtimeId))
  }

  def findByMovie(movieId: MovieId): F[List[Showtime]] = {
    ref.get.map(_.values.filter(_.movieId == movieId).toList)
  }

  def findByTheater(theaterId: TheaterId): F[List[Showtime]] = {
    ref.get.map(_.values.filter(_.theaterId == theaterId).toList)
  }

  def findByDateRange(from: LocalDateTime, to: LocalDateTime): F[List[Showtime]] = {
    ref.get.map(_.values.filter { showtime =>
      !showtime.startTime.isBefore(from) && !showtime.startTime.isAfter(to)
    }.toList)
  }

  def create(showtime: Showtime): F[Showtime] = {
    ref.modify { showtimes =>
      val updated = showtimes + (showtime.id -> showtime)
      (updated, showtime)
    }
  }

  def update(showtime: Showtime): F[Option[Showtime]] = {
    ref.modify { showtimes =>
      showtimes.get(showtime.id) match {
        case Some(_) =>
          val updated = showtimes + (showtime.id -> showtime)
          (updated, Some(showtime))
        case None =>
          (showtimes, None)
      }
    }
  }

  def delete(showtimeId: ShowtimeId): F[Boolean] = {
    ref.modify { showtimes =>
      showtimes.get(showtimeId) match {
        case Some(_) =>
          val updated = showtimes - showtimeId
          (updated, true)
        case None =>
          (showtimes, false)
      }
    }
  }

  def deleteAll(): F[Unit] = ref.set(Map.empty)
}

object InMemoryShowtimeAlgebra {
  def apply[F[_]: Sync](initialData: Map[ShowtimeId, Showtime] = Map.empty): F[ShowtimeAlgebra[F]] = {
    Ref.of[F, Map[ShowtimeId, Showtime]](initialData).map(new InMemoryShowtimeAlgebra[F](_))
  }
} 