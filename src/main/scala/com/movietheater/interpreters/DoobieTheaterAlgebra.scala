package com.movietheater.interpreters

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain._
import com.movietheater.algebras.TheaterAlgebra
import java.util.UUID

class DoobieTheaterAlgebra[F[_]: MonadCancelThrow](xa: Transactor[F]) extends TheaterAlgebra[F] {

  import DoobieTheaterAlgebra._
  import DoobieInstances._

  def findById(theaterId: TheaterId): F[Option[Theater]] = {
    selectByIdQuery(theaterId).option.transact(xa)
  }

  def findByName(name: String): F[Option[Theater]] = {
    selectByNameQuery(name).option.transact(xa)
  }

  def findAll(): F[List[Theater]] = {
    selectAllQuery.to[List].transact(xa)
  }

  def create(theater: Theater): F[Theater] = {
    insertQuery(theater).run.transact(xa).as(theater)
  }

  def update(theater: Theater): F[Option[Theater]] = {
    updateQuery(theater).run.transact(xa).map {
      case 0 => None
      case _ => Some(theater)
    }
  }

  def delete(theaterId: TheaterId): F[Boolean] = {
    deleteQuery(theaterId).run.transact(xa).map(_ > 0)
  }
}

object DoobieTheaterAlgebra {
  
  import DoobieInstances._
  
  // Row mapping for Theater
  private implicit val theaterRead: Read[Theater] = 
    Read[(UUID, String, String, Int)].map {
      case (id, name, location, capacity) =>
        Theater(TheaterId(id), name, location, capacity)
    }
  
  // SQL queries
  private def selectByIdQuery(theaterId: TheaterId): Query0[Theater] = {
    sql"""
      SELECT id, name, location, capacity 
      FROM theaters 
      WHERE id = ${theaterId.value}
    """.query[Theater]
  }

  private def selectByNameQuery(name: String): Query0[Theater] = {
    sql"""
      SELECT id, name, location, capacity 
      FROM theaters 
      WHERE name = $name
    """.query[Theater]
  }

  private val selectAllQuery: Query0[Theater] = {
    sql"""
      SELECT id, name, location, capacity 
      FROM theaters 
      ORDER BY name
    """.query[Theater]
  }

  private def insertQuery(theater: Theater): Update0 = {
    sql"""
      INSERT INTO theaters (id, name, location, capacity) 
      VALUES (${theater.id.value}, ${theater.name}, ${theater.location}, ${theater.totalSeats})
    """.update
  }

  private def updateQuery(theater: Theater): Update0 = {
    sql"""
      UPDATE theaters 
      SET name = ${theater.name}, 
          location = ${theater.location}, 
          capacity = ${theater.totalSeats},
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ${theater.id.value}
    """.update
  }

  private def deleteQuery(theaterId: TheaterId): Update0 = {
    sql"DELETE FROM theaters WHERE id = ${theaterId.value}".update
  }

  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): TheaterAlgebra[F] = 
    new DoobieTheaterAlgebra[F](xa)
} 