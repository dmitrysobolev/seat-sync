package com.movietheater.interpreters.doobie

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.AuditoriumAlgebra
import java.util.UUID

class DoobieAuditoriumAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends AuditoriumAlgebra[F] {

  import DoobieAuditoriumAlgebra._
  import DoobieInstances._

  def findById(auditoriumId: AuditoriumId): F[Option[Auditorium]] = {
    selectByIdQuery(auditoriumId).option.transact(xa)
  }

  def findByTheater(theaterId: TheaterId): F[List[Auditorium]] = {
    selectByTheaterQuery(theaterId).to[List].transact(xa)
  }

  def create(auditorium: Auditorium): F[Auditorium] = {
    insertQuery(auditorium).run.transact(xa).as(auditorium)
  }

  def update(auditorium: Auditorium): F[Option[Auditorium]] = {
    updateQuery(auditorium).run.transact(xa).map {
      case 0 => None
      case _ => Some(auditorium)
    }
  }

  def delete(auditoriumId: AuditoriumId): F[Boolean] = {
    deleteQuery(auditoriumId).run.transact(xa).map(_ > 0)
  }

  def deleteAll(): F[Unit] = {
    sql"DELETE FROM auditoriums".update.run.transact(xa).void
  }
}

object DoobieAuditoriumAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Auditorium
  private implicit val auditoriumRead: Read[Auditorium] = 
    Read[(UUID, UUID, String)].map {
      case (id, theaterId, name) =>
        Auditorium(AuditoriumId(id), TheaterId(theaterId), name)
    }
  
  // SQL queries
  private def selectByIdQuery(auditoriumId: AuditoriumId): Query0[Auditorium] = {
    sql"""
      SELECT id, theater_id, name 
      FROM auditoriums 
      WHERE id = ${auditoriumId.value}
    """.query[Auditorium]
  }

  private def selectByTheaterQuery(theaterId: TheaterId): Query0[Auditorium] = {
    sql"""
      SELECT id, theater_id, name 
      FROM auditoriums 
      WHERE theater_id = ${theaterId.value}
      ORDER BY name
    """.query[Auditorium]
  }

  private def insertQuery(auditorium: Auditorium): Update0 = {
    sql"""
      INSERT INTO auditoriums (id, theater_id, name, created_at, updated_at) 
      VALUES (${auditorium.id.value}, ${auditorium.theaterId.value}, ${auditorium.name}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    """.update
  }

  private def updateQuery(auditorium: Auditorium): Update0 = {
    sql"""
      UPDATE auditoriums 
      SET name = ${auditorium.name},
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${auditorium.id.value}
    """.update
  }

  private def deleteQuery(auditoriumId: AuditoriumId): Update0 = {
    sql"DELETE FROM auditoriums WHERE id = ${auditoriumId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): AuditoriumAlgebra[F] = 
    new DoobieAuditoriumAlgebra[F](xa)
} 