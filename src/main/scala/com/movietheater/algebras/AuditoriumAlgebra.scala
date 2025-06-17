package com.movietheater.algebras

import cats.implicits._
import com.movietheater.domain._

trait AuditoriumAlgebra[F[_]] {
  def findById(auditoriumId: AuditoriumId): F[Option[Auditorium]]
  def findByTheater(theaterId: TheaterId): F[List[Auditorium]]
  def create(auditorium: Auditorium): F[Auditorium]
  def update(auditorium: Auditorium): F[Option[Auditorium]]
  def delete(auditoriumId: AuditoriumId): F[Boolean]
  def deleteAll(): F[Unit]
}

object AuditoriumAlgebra {
  def apply[F[_]](implicit ev: AuditoriumAlgebra[F]): AuditoriumAlgebra[F] = ev
} 