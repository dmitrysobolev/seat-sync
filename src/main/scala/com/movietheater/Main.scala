package com.movietheater

import cats.effect._
import cats.implicits._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import com.comcast.ip4s._
import com.movietheater.domain._
import com.movietheater.interpreters._
import com.movietheater.services.ReservationService
import com.movietheater.http.routes.ReservationRoutes
import java.util.UUID
import java.time.LocalDateTime

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    createServer[IO].useForever
  }

  def createServer[F[_]: Async]: Resource[F, org.http4s.server.Server] = {
    for {
      // Create sample data
      sampleData <- Resource.eval(createSampleData[F])
      (movies, theaters, showtimes, seats, tickets, customers) = sampleData

      // Create algebras (in-memory implementations)
      movieAlgebra <- Resource.eval(InMemoryMovieAlgebra[F](movies))
      theaterAlgebra <- Resource.eval(InMemoryTheaterAlgebra[F](theaters))
      showtimeAlgebra <- Resource.eval(InMemoryShowtimeAlgebra[F](showtimes))
      customerAlgebra <- Resource.eval(InMemoryCustomerAlgebra[F](customers))
      ticketAlgebra <- Resource.eval(InMemoryTicketAlgebra[F](tickets))
      seatAlgebra <- Resource.eval(InMemorySeatAlgebra[F](ticketAlgebra, seats))

      // Create service
      reservationService = ReservationService[F](
        movieAlgebra,
        theaterAlgebra,
        showtimeAlgebra,
        seatAlgebra,
        ticketAlgebra,
        customerAlgebra
      )

      // Create routes
      reservationRoutes = ReservationRoutes[F](reservationService)

      // Combine all routes
      httpApp = reservationRoutes.routes.orNotFound

      // Add logging middleware
      finalApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)

      // Create server
      server <- EmberServerBuilder.default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(finalApp)
        .build
    } yield server
  }

  def createSampleData[F[_]: Sync]: F[(
    Map[MovieId, Movie],
    Map[TheaterId, Theater],
    Map[ShowtimeId, Showtime],
    Map[SeatId, Seat],
    Map[TicketId, Ticket],
    Map[CustomerId, Customer]
  )] = {
    Sync[F].delay {
      // Sample movies
      val movie1Id = MovieId(UUID.randomUUID())
      val movie2Id = MovieId(UUID.randomUUID())
      val movies = Map(
        movie1Id -> Movie(movie1Id, "The Matrix", "A computer programmer discovers reality is a simulation", 136, "R"),
        movie2Id -> Movie(movie2Id, "Inception", "A thief enters people's dreams to steal secrets", 148, "PG-13")
      )

      // Sample theaters
      val theater1Id = TheaterId(UUID.randomUUID())
      val theater2Id = TheaterId(UUID.randomUUID())
      val theaters = Map(
        theater1Id -> Theater(theater1Id, "Cinema One", "Downtown Mall", 100),
        theater2Id -> Theater(theater2Id, "Grand Theater", "Uptown Plaza", 150)
      )

      // Sample seats
      val seats1 = (1 to 10).flatMap { row =>
        (1 to 10).map { number =>
          val seatId = SeatId(s"A$row-$number")
          val seatType = if (row <= 3) SeatType.VIP else if (row <= 6) SeatType.Premium else SeatType.Regular
          seatId -> Seat(seatId, s"A$row", number, theater1Id, seatType)
        }
      }.toMap

      val seats2 = (1 to 15).flatMap { row =>
        (1 to 10).map { number =>
          val seatId = SeatId(s"B$row-$number")
          val seatType = if (row <= 3) SeatType.VIP else if (row <= 8) SeatType.Premium else SeatType.Regular
          seatId -> Seat(seatId, s"B$row", number, theater2Id, seatType)
        }
      }.toMap

      val allSeats = seats1 ++ seats2

      // Sample showtimes
      val now = LocalDateTime.now()
      val showtime1Id = ShowtimeId(UUID.randomUUID())
      val showtime2Id = ShowtimeId(UUID.randomUUID())
      val showtime3Id = ShowtimeId(UUID.randomUUID())
      val showtimes = Map(
        showtime1Id -> Showtime(showtime1Id, movie1Id, theater1Id, now.plusHours(2), now.plusHours(4), BigDecimal("12.50")),
        showtime2Id -> Showtime(showtime2Id, movie2Id, theater1Id, now.plusHours(5), now.plusHours(7), BigDecimal("15.00")),
        showtime3Id -> Showtime(showtime3Id, movie1Id, theater2Id, now.plusHours(3), now.plusHours(5), BigDecimal("14.00"))
      )

      // Sample customers
      val customer1Id = CustomerId(UUID.randomUUID())
      val customer2Id = CustomerId(UUID.randomUUID())
      val customers = Map(
        customer1Id -> Customer(customer1Id, "john@example.com", "John", "Doe"),
        customer2Id -> Customer(customer2Id, "jane@example.com", "Jane", "Smith")
      )

      // No tickets initially
      val tickets = Map.empty[TicketId, Ticket]

      (movies, theaters, showtimes, allSeats, tickets, customers)
    }
  }
} 