package com.movietheater.algebras

import cats.effect.IO
import com.movietheater.domain._
import com.movietheater.interpreters.inmemory.InMemoryAuditoriumAlgebra
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import cats.effect.unsafe.implicits.global
import java.util.UUID

class AuditoriumAlgebraSuite extends AnyFunSuite with Matchers {
  test("AuditoriumAlgebra - create and findById") {
    val auditorium = Auditorium(AuditoriumId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), "Auditorium 1")

    val program = for {
      algebra <- InMemoryAuditoriumAlgebra[IO]()
      _ <- algebra.create(auditorium)
      retrieved <- algebra.findById(auditorium.id)
    } yield retrieved

    program.unsafeRunSync() should be(Some(auditorium))
  }

  test("AuditoriumAlgebra - findByTheater") {
    val theaterId = TheaterId(UUID.randomUUID())
    val auditorium1 = Auditorium(AuditoriumId(UUID.randomUUID()), theaterId, "Auditorium 1")
    val auditorium2 = Auditorium(AuditoriumId(UUID.randomUUID()), theaterId, "Auditorium 2")
    val auditorium3 = Auditorium(AuditoriumId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), "Auditorium 3")

    val program = for {
      algebra <- InMemoryAuditoriumAlgebra[IO](Map(auditorium1.id -> auditorium1, auditorium2.id -> auditorium2, auditorium3.id -> auditorium3))
      retrieved <- algebra.findByTheater(theaterId)
    } yield retrieved

    program.unsafeRunSync() should be(List(auditorium1, auditorium2))
  }

  test("AuditoriumAlgebra - update") {
    val auditorium = Auditorium(AuditoriumId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), "Auditorium 1")
    val updatedAuditorium = auditorium.copy(name = "Updated Auditorium 1")

    val program = for {
      algebra <- InMemoryAuditoriumAlgebra[IO](Map(auditorium.id -> auditorium))
      _ <- algebra.update(updatedAuditorium)
      retrieved <- algebra.findById(auditorium.id)
    } yield retrieved

    program.unsafeRunSync() should be(Some(updatedAuditorium))
  }

  test("AuditoriumAlgebra - delete") {
    val auditorium = Auditorium(AuditoriumId(UUID.randomUUID()), TheaterId(UUID.randomUUID()), "Auditorium 1")

    val program = for {
      algebra <- InMemoryAuditoriumAlgebra[IO](Map(auditorium.id -> auditorium))
      _ <- algebra.delete(auditorium.id)
      retrieved <- algebra.findById(auditorium.id)
    } yield retrieved

    program.unsafeRunSync() should be(None)
  }

  test("AuditoriumAlgebra - deleteAll") {
    val theaterId = TheaterId(UUID.randomUUID())
    val auditorium1 = Auditorium(AuditoriumId(UUID.randomUUID()), theaterId, "Auditorium 1")
    val auditorium2 = Auditorium(AuditoriumId(UUID.randomUUID()), theaterId, "Auditorium 2")

    val program = for {
      algebra <- InMemoryAuditoriumAlgebra[IO](Map(auditorium1.id -> auditorium1, auditorium2.id -> auditorium2))
      _ <- algebra.deleteAll()
      retrieved <- algebra.findByTheater(theaterId)
    } yield retrieved

    program.unsafeRunSync() should be(List.empty)
  }
}
