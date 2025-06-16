package com.movietheater.http

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.movietheater.algebras._
import com.movietheater.domain._
import com.movietheater.interpreters.doobie._
import com.movietheater.interpreters.inmemory._
import com.movietheater.services.{ReservationService, SeatStatusSyncService}
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec._
import io.circe.syntax._
import java.time.LocalDateTime
import java.util.UUID
import org.http4s.circe._
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class HttpRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "ReservationRoutes" - {
    "should be constructible" in {
      val test = for {
        movieAlgebra <- InMemoryMovieAlgebra[IO]()
        theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
        showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
        customerAlgebra <- InMemoryCustomerAlgebra[IO]()
        ticketAlgebra <- InMemoryTicketAlgebra[IO]()
        seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
        
        service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
        routes = ReservationRoutes[IO](service)
      } yield routes

      test.asserting { routes =>
        routes shouldBe a[ReservationRoutes[IO]]
      }
    }

    "should handle invalid JSON gracefully" in {
      val test = for {
        movieAlgebra <- InMemoryMovieAlgebra[IO]()
        theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
        showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
        customerAlgebra <- InMemoryCustomerAlgebra[IO]()
        ticketAlgebra <- InMemoryTicketAlgebra[IO]()
        seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
        
        service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
        routes = ReservationRoutes[IO](service)
        
        response <- routes.routes.orNotFound.run(
          Request[IO](Method.POST, uri"/reservations")
            .withEntity("{invalid json}")
        ).attempt
      } yield response

      test.asserting { result =>
        result.isLeft shouldBe true
      }
    }

    "should return 404 for non-existent routes" in {
      val test = for {
        movieAlgebra <- InMemoryMovieAlgebra[IO]()
        theaterAlgebra <- InMemoryTheaterAlgebra[IO]()
        showtimeAlgebra <- InMemoryShowtimeAlgebra[IO]()
        customerAlgebra <- InMemoryCustomerAlgebra[IO]()
        ticketAlgebra <- InMemoryTicketAlgebra[IO]()
        seatAlgebra <- InMemorySeatAlgebra[IO](ticketAlgebra)
        
        service = ReservationService[IO](movieAlgebra, theaterAlgebra, showtimeAlgebra, seatAlgebra, ticketAlgebra, customerAlgebra)
        routes = ReservationRoutes[IO](service)
        
        response <- routes.routes.orNotFound.run(
          Request[IO](Method.GET, uri"/nonexistent")
        )
      } yield response

      test.asserting { response =>
        response.status shouldBe Status.NotFound
      }
    }
  }
} 