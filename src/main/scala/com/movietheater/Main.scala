package com.movietheater

import cats.effect._
import cats.implicits._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger
import com.comcast.ip4s._
import com.movietheater.domain._
import com.movietheater.config._
import com.movietheater.interpreters._
import com.movietheater.services.ReservationService
import com.movietheater.http.routes.ReservationRoutes
import com.movietheater.db.Database
import pureconfig._
import pureconfig.generic.derivation.default._
import java.util.UUID
import java.time.LocalDateTime
import doobie.Transactor

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
      // Check if data already exists
      existingMovies <- movieAlgebra.findAll()
      _ <- if (existingMovies.isEmpty) {
        for {
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
      } else {
        Async[F].unit
      }
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
      val theaters = Map(
        theater1Id -> Theater(theater1Id, "Cinema One", "Downtown Mall", 100),
        theater2Id -> Theater(theater2Id, "Grand Theater", "Uptown Plaza", 150)
      )

      // Sample seats - fix constructor parameters
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

      // Sample showtimes - fix constructor to include endTime
      val now = LocalDateTime.now()
      val showtime1Id = ShowtimeId(UUID.randomUUID())
      val showtime2Id = ShowtimeId(UUID.randomUUID())
      val showtime3Id = ShowtimeId(UUID.randomUUID())
      val showtimes = Map(
        showtime1Id -> Showtime(showtime1Id, movie1Id, theater1Id, now.plusHours(2), now.plusHours(4).plusMinutes(16), BigDecimal("12.50")),
        showtime2Id -> Showtime(showtime2Id, movie2Id, theater1Id, now.plusHours(5), now.plusHours(7).plusMinutes(28), BigDecimal("15.00")),
        showtime3Id -> Showtime(showtime3Id, movie1Id, theater2Id, now.plusHours(3), now.plusHours(5).plusMinutes(16), BigDecimal("14.00"))
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

  // Keep the original methods for compatibility
  def createServer[F[_]: Async]: Resource[F, org.http4s.server.Server] = {
    createServerWithInMemory[F](ServerConfig("0.0.0.0", 8080))
  }
} 