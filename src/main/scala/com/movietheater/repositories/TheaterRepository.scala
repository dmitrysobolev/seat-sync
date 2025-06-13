package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.postgres.implicits._
import com.movietheater.domain.{Theater, TheaterId}
import com.movietheater.db.DoobieInstances._

class TheaterRepository(xa: doobie.Transactor[IO]) {
  def findById(theaterId: TheaterId): IO[Option[Theater]] = {
    sql"""
      SELECT id, name, location, total_seats
      FROM theaters
      WHERE id = $theaterId
    """.query[Theater].option.transact(xa)
  }

  def findAll: IO[List[Theater]] = {
    sql"""
      SELECT id, name, location, total_seats
      FROM theaters
    """.query[Theater].stream.compile.toList.transact(xa)
  }

  def create(theater: Theater): IO[Theater] = {
    sql"""
      INSERT INTO theaters (id, name, location, total_seats)
      VALUES (${theater.id}, ${theater.name}, ${theater.location}, ${theater.totalSeats})
    """.update.run.transact(xa).map(_ => theater)
  }

  def update(theater: Theater): IO[Theater] = {
    sql"""
      UPDATE theaters
      SET name = ${theater.name},
          location = ${theater.location},
          total_seats = ${theater.totalSeats}
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