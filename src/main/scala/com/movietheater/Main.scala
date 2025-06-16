package com.movietheater

import cats.effect.{IO, IOApp}
import com.movietheater.algebras._
import com.movietheater.config.AppConfig
import com.movietheater.http.HttpRoutes
import com.movietheater.interpreters.doobie._
import com.movietheater.interpreters.inmemory._
import com.movietheater.services.{ReservationService, SeatStatusSyncService}
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{Logger, RequestId}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import com.comcast.ip4s._
import com.movietheater.domain._
import java.util.UUID
import java.time.LocalDateTime

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    createServerFromConfig[IO].useForever
  }

  def createServerFromConfig[F[_]: Async]: Resource[F, org.http4s.server.Server] = {
    for {
      // Load configuration
      config <- Resource.eval(loadConfig[F])
      
      // Choose between InMemory and PostgreSQL based on environment
      usePostgres = sys.env.get("USE_POSTGRES").exists(_.toBoolean)
      
      server <- if (usePostgres) {
        createServerWithPostgres[F](config)
      } else {
        createServerWithInMemory[F](config.server)
      }
    } yield server
  }

  def createServerWithPostgres[F[_]: Async](config: AppConfig): Resource[F, org.http4s.server.Server] = {
    for {
      // Run database migrations
      _ <- Resource.eval(Database.runMigrations[F](config.database))
      
      // Create database transactor
      xa <- Database.createTransactor[F](config.database)
      
      // Create all Doobie algebras
      movieAlgebra = DoobieMovieAlgebra[F](xa)
      theaterAlgebra = DoobieTheaterAlgebra[F](xa)
      showtimeAlgebra = DoobieShowtimeAlgebra[F](xa)
      ticketAlgebra = DoobieTicketAlgebra[F](xa)
      seatAlgebra = DoobieSeatAlgebra[F](xa, ticketAlgebra)
      customerAlgebra = DoobieCustomerAlgebra[F](xa)

      // Populate database with sample data
      _ <- Resource.eval(populateDatabase[F](movieAlgebra, theaterAlgebra, seatAlgebra, showtimeAlgebra, customerAlgebra))

      // Create service and server
      server <- createServerWithAlgebras[F](
        config.server,
        movieAlgebra,
        theaterAlgebra,
        showtimeAlgebra,
        seatAlgebra,
        ticketAlgebra,
        customerAlgebra
      )
    } yield server
  }

  def createServerWithInMemory[F[_]: Async](serverConfig: ServerConfig): Resource[F, org.http4s.server.Server] = {
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

      // Create service and server
      server <- createServerWithAlgebras[F](
        serverConfig,
        movieAlgebra,
        theaterAlgebra,
        showtimeAlgebra,
        seatAlgebra,
        ticketAlgebra,
        customerAlgebra
      )
    } yield server
  }

  def createServerWithAlgebras[F[_]: Async](
    serverConfig: ServerConfig,
    movieAlgebra: com.movietheater.algebras.MovieAlgebra[F],
    theaterAlgebra: com.movietheater.algebras.TheaterAlgebra[F],
    showtimeAlgebra: com.movietheater.algebras.ShowtimeAlgebra[F],
    seatAlgebra: com.movietheater.algebras.SeatAlgebra[F],
    ticketAlgebra: com.movietheater.algebras.TicketAlgebra[F],
    customerAlgebra: com.movietheater.algebras.CustomerAlgebra[F]
  ): Resource[F, org.http4s.server.Server] = {
    for {
      // Create service
      reservationService <- Resource.pure(ReservationService[F](
        movieAlgebra,
        theaterAlgebra,
        showtimeAlgebra,
        seatAlgebra,
        ticketAlgebra,
        customerAlgebra
      ))

      // Create routes
      reservationRoutes <- Resource.pure(ReservationRoutes[F](reservationService))

      // Combine all routes
      httpApp <- Resource.pure(reservationRoutes.routes.orNotFound)

      // Add logging middleware
      finalApp <- Resource.pure(Logger.httpApp(logHeaders = true, logBody = true)(httpApp))

      // Create server
      server <- EmberServerBuilder.default[F]
        .withHost(Host.fromString(serverConfig.host).getOrElse(ipv4"0.0.0.0"))
        .withPort(Port.fromInt(serverConfig.port).getOrElse(port"8080"))
        .withHttpApp(finalApp)
        .build
    } yield server
  }

  def loadConfig[F[_]: Sync]: F[AppConfig] = {
    Sync[F].delay {
      ConfigSource.default.loadOrThrow[AppConfig]
    }
  }

  def populateDatabase[F[_]: Async](
    movieAlgebra: com.movietheater.algebras.MovieAlgebra[F],
    theaterAlgebra: com.movietheater.algebras.TheaterAlgebra[F],
    seatAlgebra: com.movietheater.algebras.SeatAlgebra[F],
    showtimeAlgebra: com.movietheater.algebras.ShowtimeAlgebra[F],
    customerAlgebra: com.movietheater.algebras.CustomerAlgebra[F]
  ): F[Unit] = {
    for {
      // Clear existing data
      _ <- movieAlgebra.deleteAll()
      _ <- theaterAlgebra.deleteAll()
      _ <- customerAlgebra.deleteAll()
      _ <- seatAlgebra.deleteAll()
      _ <- showtimeAlgebra.deleteAll()

      // Create new sample data
      sampleData <- createSampleData[F]
      (movies, theaters, showtimes, seats, _, customers) = sampleData
      
      // Create movies
      _ <- movies.values.toList.traverse(movieAlgebra.create)
      
      // Create theaters
      _ <- theaters.values.toList.traverse(theaterAlgebra.create)
      
      // Create customers  
      _ <- customers.values.toList.traverse(customerAlgebra.create)
      
      // Create seats
      _ <- seats.values.toList.traverse(seatAlgebra.create)
      
      // Create showtimes
      _ <- showtimes.values.toList.traverse(showtimeAlgebra.create)
    } yield ()
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
      val now = LocalDateTime.now()
      val theaters = Map(
        theater1Id -> Theater(theater1Id, "Cinema One", "Downtown Mall", now, now),
        theater2Id -> Theater(theater2Id, "Grand Theater", "Uptown Plaza", now, now)
      )

      // Sample auditoriums
      val auditorium1Id = AuditoriumId(UUID.randomUUID())
      val auditorium2Id = AuditoriumId(UUID.randomUUID())
      val auditorium3Id = AuditoriumId(UUID.randomUUID())
      val auditoriums = Map(
        auditorium1Id -> Auditorium(auditorium1Id, theater1Id, "Auditorium 1", now, now),
        auditorium2Id -> Auditorium(auditorium2Id, theater1Id, "Auditorium 2", now, now),
        auditorium3Id -> Auditorium(auditorium3Id, theater2Id, "Main Auditorium", now, now)
      )

      // Sample seats - fix constructor parameters
      val seats1 = (1 to 10).flatMap { row =>
        (1 to 10).map { number =>
          val seatId = SeatId(s"A$row-$number")
          Seat(seatId, auditorium1Id, s"A$row", number, now, now)
        }
      }.toList
      val seats2 = (1 to 10).flatMap { row =>
        (1 to 10).map { number =>
          val seatId = SeatId(s"B$row-$number")
          Seat(seatId, auditorium2Id, s"B$row", number, now, now)
        }
      }.toList
      val seats3 = (1 to 15).flatMap { row =>
        (1 to 10).map { number =>
          val seatId = SeatId(s"C$row-$number")
          Seat(seatId, auditorium3Id, s"C$row", number, now, now)
        }
      }.toList
      val allSeats = (seats1 ++ seats2 ++ seats3).map(s => s.id -> s).toMap

      // Sample showtimes - fix constructor to include endTime
      val showtime1Id = ShowtimeId(UUID.randomUUID())
      val showtime2Id = ShowtimeId(UUID.randomUUID())
      val showtime3Id = ShowtimeId(UUID.randomUUID())
      val showtimes = Map(
        showtime1Id -> Showtime(
          showtime1Id,
          movie1Id,
          theater1Id,
          auditorium1Id,
          now,
          now.plusHours(2),
          Map.empty, // seatTypes
          Map(
            SeatType.Regular -> Money.fromDollars(12, 50),
            SeatType.Premium -> Money.fromDollars(18, 75),
            SeatType.VIP -> Money.fromDollars(25, 0)
          ),
          now,
          now
        ),
        showtime2Id -> Showtime(
          showtime2Id,
          movie2Id,
          theater2Id,
          auditorium2Id,
          now,
          now.plusHours(5),
          Map.empty,
          Map(
            SeatType.Regular -> Money.fromDollars(15, 0),
            SeatType.Premium -> Money.fromDollars(22, 50),
            SeatType.VIP -> Money.fromDollars(30, 0)
          ),
          now,
          now
        ),
        showtime3Id -> Showtime(
          showtime3Id,
          movie1Id,
          theater2Id,
          auditorium3Id,
          now,
          now.plusHours(3),
          Map.empty,
          Map(
            SeatType.Regular -> Money.fromDollars(14, 0),
            SeatType.Premium -> Money.fromDollars(21, 0),
            SeatType.VIP -> Money.fromDollars(28, 0)
          ),
          now,
          now
        )
      )

      // Sample customers
      val customer1Id = CustomerId(UUID.randomUUID())
      val customer2Id = CustomerId(UUID.randomUUID())
      val customers = Map(
        customer1Id -> Customer(customer1Id, "John Doe", "john@example.com", now, now),
        customer2Id -> Customer(customer2Id, "Jane Smith", "jane@example.com", now, now)
      )

      // No tickets initially
      val tickets = Map.empty[TicketId, Ticket]

      (movies, theaters, showtimes, allSeats, tickets, customers)
    }
  }

  // Keep the original methods for compatibility
  def createServer[F[_]: Async]: Resource[F, org.http4s.server.Server] = {
    createServerWithInMemory[F](ServerConfig("0.0.0.0", 8080))
  }
} 