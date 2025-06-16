package com.movietheater

import cats.effect.{IO, IOApp, ExitCode, Async, Sync, Resource}
import cats.implicits._
import com.movietheater.algebras._
import com.movietheater.config.{AppConfig, ServerConfig}
import com.movietheater.db.Database
import com.movietheater.interpreters.doobie._
import com.movietheater.interpreters.inmemory._
import com.movietheater.services.{ReservationService, SeatStatusSyncService}
import com.movietheater.http.routes.ReservationRoutes
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{Logger, RequestId}
import pureconfig.ConfigSource
import pureconfig.generic.derivation.default._
import com.comcast.ip4s._
import com.movietheater.domain._
import java.util.UUID
import java.time.{LocalDateTime, Duration}

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
      seatStatusSyncService <- Resource.pure(SeatStatusSyncService[F](
        showtimeAlgebra,
        ticketAlgebra,
      ))
      reservationService <- Resource.pure(ReservationService[F](
        movieAlgebra,
        theaterAlgebra,
        showtimeAlgebra,
        seatAlgebra,
        ticketAlgebra,
        customerAlgebra,
        seatStatusSyncService
      ))

      reservationRoutes <- Resource.pure(ReservationRoutes[F](reservationService))

      httpApp <- Resource.pure(reservationRoutes.routes.orNotFound)

      finalApp <- Resource.pure(Logger.httpApp(logHeaders = true, logBody = true)(httpApp))

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
      val now = LocalDateTime.now()
      val movie1Id = MovieId(UUID.randomUUID())
      val movie2Id = MovieId(UUID.randomUUID())
      val movies = Map(
        movie1Id -> Movie(movie1Id, "The Matrix", "A computer programmer discovers reality is a simulation", Duration.ofMinutes(136), "R", now, now),
        movie2Id -> Movie(movie2Id, "Inception", "A thief enters people's dreams to steal secrets", Duration.ofMinutes(148), "PG-13", now, now)
      )

      val theater1Id = TheaterId(UUID.randomUUID())
      val theater2Id = TheaterId(UUID.randomUUID())
      val theaters = Map(
        theater1Id -> Theater(theater1Id, "Cinema One", "Downtown Mall", 100, now, now),
        theater2Id -> Theater(theater2Id, "Grand Theater", "Uptown Plaza", 200, now, now)
      )

      val auditorium1Id = AuditoriumId(UUID.randomUUID())
      val auditorium2Id = AuditoriumId(UUID.randomUUID())
      val auditorium3Id = AuditoriumId(UUID.randomUUID())
      val auditoriums = Map(
        auditorium1Id -> Auditorium(auditorium1Id, theater1Id, "Auditorium 1", now, now),
        auditorium2Id -> Auditorium(auditorium2Id, theater1Id, "Auditorium 2", now, now),
        auditorium3Id -> Auditorium(auditorium3Id, theater2Id, "Main Auditorium", now, now)
      )

      val seats1 = ('A' to 'P').flatMap { row =>
        (1 to 25).map { number =>
          val seatId = SeatId(s"$row$number")
          Seat(seatId, theater1Id, auditorium1Id, RowNumber(row), SeatNumber(number), now, now)
        }
      }.toList
      val seats2 = ('A' to 'P').flatMap { row =>
        (1 to 25).map { number =>
          val seatId = SeatId(s"$row$number")
          Seat(seatId, theater1Id, auditorium2Id, RowNumber(row), SeatNumber(number), now, now)
        }
      }.toList
      val seats3 = ('A' to 'P').flatMap { row =>
        (1 to 25).map { number =>
          val seatId = SeatId(s"$row$number")
          Seat(seatId, theater2Id, auditorium3Id, RowNumber(row), SeatNumber(number), now, now)
        }
      }.toList
      val allSeats = (seats1 ++ seats2 ++ seats3).map(s => s.id -> s).toMap

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
          Map(
            SeatId("A1") -> SeatType.Standard,
            SeatId("A2") -> SeatType.Standard,
            SeatId("A3") -> SeatType.Premium,
            SeatId("A4") -> SeatType.Premium,
            SeatId("A5") -> SeatType.VIP,
          ),
          Map(
            SeatType.Standard -> Money.fromDollars(12, 50),
            SeatType.Premium -> Money.fromDollars(18, 75),
            SeatType.VIP -> Money.fromDollars(25, 0)
          ),
          Map(
            SeatId("A1") -> SeatStatus.Available,
            SeatId("A2") -> SeatStatus.Available,
            SeatId("A3") -> SeatStatus.Available,
            SeatId("A4") -> SeatStatus.Available,
            SeatId("A5") -> SeatStatus.Available
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
          Map(
            SeatId("A1") -> SeatType.Standard,
            SeatId("A2") -> SeatType.Standard,
            SeatId("A3") -> SeatType.Premium,
            SeatId("A4") -> SeatType.Premium,
            SeatId("A5") -> SeatType.VIP,
          ),
          Map(
            SeatType.Standard -> Money.fromDollars(12, 50),
            SeatType.Premium -> Money.fromDollars(18, 75),
            SeatType.VIP -> Money.fromDollars(25, 0)
          ),
          Map(
            SeatId("A1") -> SeatStatus.Available,
            SeatId("A2") -> SeatStatus.Available,
            SeatId("A3") -> SeatStatus.Available,
            SeatId("A4") -> SeatStatus.Available,
            SeatId("A5") -> SeatStatus.Available
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
          Map(
            SeatId("A1") -> SeatType.Standard,
            SeatId("A2") -> SeatType.Standard,
            SeatId("A3") -> SeatType.Premium,
            SeatId("A4") -> SeatType.Premium,
            SeatId("A5") -> SeatType.VIP,
          ),
          Map(
            SeatType.Standard -> Money.fromDollars(12, 50),
            SeatType.Premium -> Money.fromDollars(18, 75),
            SeatType.VIP -> Money.fromDollars(25, 0)
          ),
          Map(
            SeatId("A1") -> SeatStatus.Available,
            SeatId("A2") -> SeatStatus.Available,
            SeatId("A3") -> SeatStatus.Available,
            SeatId("A4") -> SeatStatus.Available,
            SeatId("A5") -> SeatStatus.Available
          ),
          now,
          now
        )
      )

      // Sample customers
      val customer1Id = CustomerId(UUID.randomUUID())
      val customer2Id = CustomerId(UUID.randomUUID())
      val customers = Map(
        customer1Id -> Customer(customer1Id, "john@example.com", "John", "Doe", now, now),
        customer2Id -> Customer(customer2Id, "jane@example.com", "Jane", "Doe", now, now)
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