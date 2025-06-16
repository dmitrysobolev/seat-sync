package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Theater, TheaterId}
import com.movietheater.db.DoobieInstances._
import doobie.util.{Get, Put}
import java.time.LocalDateTime

class TheaterRepository(xa: doobie.Transactor[IO]) {
  // Doobie typeclass instances
  implicit val get: Get[Theater] = (
    Get[TheaterId].map(_.asInstanceOf[TheaterId]) <*>
      Get[String] <*>
      Get[String] <*>
      Get[Int] <*>
      Get[LocalDateTime] <*>
      Get[LocalDateTime]
    ).map {
    case (id, name, address, totalSeats, createdAt, updatedAt) =>
      Theater(id, name, address, totalSeats, createdAt, updatedAt)
  }

  implicit val put: Put[Theater] = (
    Put[TheaterId] <*>
      Put[String] <*>
      Put[String] <*>
      Put[Int] <*>
      Put[LocalDateTime] <*>
      Put[LocalDateTime]
    ).contramap { theater =>
    (
      theater.id,
      theater.name,
      theater.address,
      theater.totalSeats,
      theater.createdAt,
      theater.updatedAt
    )
  }

  def findById(theaterId: TheaterId): IO[Option[Theater]] = {
    sql"""
      SELECT id, name, address, total_seats, created_at, updated_at
      FROM theaters
      WHERE id = $theaterId
    """.query[Theater].option.transact(xa)
  }

  def findAll: IO[List[Theater]] = {
    sql"""
      SELECT id, name, address, total_seats, created_at, updated_at
      FROM theaters
    """.query[Theater].stream.compile.toList.transact(xa)
  }

  def create(theater: Theater): IO[Theater] = {
    sql"""
      INSERT INTO theaters (id, name, address, total_seats, created_at, updated_at)
      VALUES (${theater.id}, ${theater.name}, ${theater.address}, ${theater.totalSeats}, ${theater.createdAt}, ${theater.updatedAt})
    """.update.run.transact(xa).map(_ => theater)
  }

  def update(theater: Theater): IO[Theater] = {
    sql"""
      UPDATE theaters
      SET name = ${theater.name},
          address = ${theater.address},
          total_seats = ${theater.totalSeats},
          updated_at = ${theater.updatedAt}
      WHERE id = ${theater.id}
    """.update.run.transact(xa).map(_ => theater)
  }

  def delete(theaterId: TheaterId): IO[Unit] = {
    sql"""
      DELETE FROM theaters
      WHERE id = $theaterId
    """.update.run.transact(xa).void
  }

  def deleteAll: IO[Unit] = {
    sql"""
      DELETE FROM theaters
    """.update.run.transact(xa).void
  }
} 