package com.movietheater.interpreters.inmemory

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.AuditoriumAlgebra

class InMemoryAuditoriumAlgebra[F[_]: Sync](ref: Ref[F, Map[AuditoriumId, Auditorium]]) extends AuditoriumAlgebra[F] {

  def findById(auditoriumId: AuditoriumId): F[Option[Auditorium]] = {
    ref.get.map(_.get(auditoriumId))
  }

  def findByTheater(theaterId: TheaterId): F[List[Auditorium]] = {
    ref.get.map(_.values.filter(_.theaterId == theaterId).toList)
  }

  def create(auditorium: Auditorium): F[Auditorium] = {
    ref.modify { auditoriums =>
      val updated = auditoriums + (auditorium.id -> auditorium)
      (updated, auditorium)
    }
  }

  def update(auditorium: Auditorium): F[Option[Auditorium]] = {
    ref.modify { auditoriums =>
      auditoriums.get(auditorium.id) match {
        case Some(_) =>
          val updated = auditoriums + (auditorium.id -> auditorium)
          (updated, Some(auditorium))
        case None =>
          (auditoriums, None)
      }
    }
  }

  def delete(auditoriumId: AuditoriumId): F[Boolean] = {
    ref.modify { auditoriums =>
      auditoriums.get(auditoriumId) match {
        case Some(_) =>
          val updated = auditoriums - auditoriumId
          (updated, true)
        case None =>
          (auditoriums, false)
      }
    }
  }

  def deleteAll(): F[Unit] = {
    ref.set(Map.empty)
  }
}

object InMemoryAuditoriumAlgebra {
  def apply[F[_]: Sync](initialData: Map[AuditoriumId, Auditorium] = Map.empty): F[AuditoriumAlgebra[F]] = {
    Ref.of[F, Map[AuditoriumId, Auditorium]](initialData).map(new InMemoryAuditoriumAlgebra[F](_))
  }
}
