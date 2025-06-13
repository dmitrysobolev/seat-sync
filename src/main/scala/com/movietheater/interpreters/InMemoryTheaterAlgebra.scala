package com.movietheater.interpreters

import cats.effect.Sync
import cats.effect.Ref
import cats.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.TheaterAlgebra

class InMemoryTheaterAlgebra[F[_]: Sync](ref: Ref[F, Map[TheaterId, Theater]]) extends TheaterAlgebra[F] {

  def findById(theaterId: TheaterId): F[Option[Theater]] = {
    ref.get.map(_.get(theaterId))
  }

  def findAll(): F[List[Theater]] = {
    ref.get.map(_.values.toList)
  }

  def create(theater: Theater): F[Theater] = {
    ref.modify { theaters =>
      val updated = theaters + (theater.id -> theater)
      (updated, theater)
    }
  }

  def update(theater: Theater): F[Option[Theater]] = {
    ref.modify { theaters =>
      theaters.get(theater.id) match {
        case Some(_) =>
          val updated = theaters + (theater.id -> theater)
          (updated, Some(theater))
        case None =>
          (theaters, None)
      }
    }
  }

  def delete(theaterId: TheaterId): F[Boolean] = {
    ref.modify { theaters =>
      theaters.get(theaterId) match {
        case Some(_) =>
          val updated = theaters - theaterId
          (updated, true)
        case None =>
          (theaters, false)
      }
    }
  }

  def deleteAll(): F[Unit] = ref.set(Map.empty)
}

object InMemoryTheaterAlgebra {
  def apply[F[_]: Sync](initialData: Map[TheaterId, Theater] = Map.empty): F[TheaterAlgebra[F]] = {
    Ref.of[F, Map[TheaterId, Theater]](initialData).map(new InMemoryTheaterAlgebra[F](_))
  }
} 