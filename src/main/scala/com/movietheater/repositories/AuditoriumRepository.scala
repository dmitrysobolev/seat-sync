package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Auditorium, AuditoriumId, TheaterId}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}
import java.time.LocalDateTime

class AuditoriumRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Auditorium] = (
    Get[AuditoriumId].map(_.asInstanceOf[AuditoriumId]) <*>
      Get[TheaterId].map(_.asInstanceOf[TheaterId]) <*>
      Get[String] <*>
      Get[LocalDateTime] <*>
      Get[LocalDateTime]
    ).map {
    case (id, theaterId, name, createdAt, updatedAt) =>
      Auditorium(id, theaterId, name, createdAt, updatedAt)
  }

  implicit val put: Put[Auditorium] = (
    Put[AuditoriumId] <*>
      Put[TheaterId] <*>
      Put[String] <*>
      Put[LocalDateTime] <*>
      Put[LocalDateTime]
    ).contramap { auditorium =>
    (
      auditorium.id,
      auditorium.theaterId,
      auditorium.name,
      auditorium.createdAt,
      auditorium.updatedAt
    )
  }

  def findById(id: AuditoriumId): IO[Option[Auditorium]] = {
    sql"""
      SELECT id, theater_id, name, created_at, updated_at
      FROM auditoriums
      WHERE id = $id
    """.query[Auditorium].option.transact(xa)
  }

  def findByTheaterId(theaterId: TheaterId): IO[List[Auditorium]] = {
    sql"""
      SELECT id, theater_id, name, created_at, updated_at
      FROM auditoriums
      WHERE theater_id = $theaterId
    """.query[Auditorium].stream.compile.toList.transact(xa)
  }

  def create(auditorium: Auditorium): IO[Auditorium] = {
    sql"""
      INSERT INTO auditoriums (id, theater_id, name, created_at, updated_at)
      VALUES (${auditorium.id}, ${auditorium.theaterId}, ${auditorium.name}, 
              ${auditorium.createdAt}, ${auditorium.updatedAt})
    """.update.run.transact(xa).map(_ => auditorium)
  }

  def update(auditorium: Auditorium): IO[Auditorium] = {
    sql"""
      UPDATE auditoriums
      SET name = ${auditorium.name},
          updated_at = ${auditorium.updatedAt}
      WHERE id = ${auditorium.id}
    """.update.run.transact(xa).map(_ => auditorium)
  }

  def delete(id: AuditoriumId): IO[Unit] = {
    sql"""
      DELETE FROM auditoriums
      WHERE id = $id
    """.update.run.transact(xa).void
  }
} 