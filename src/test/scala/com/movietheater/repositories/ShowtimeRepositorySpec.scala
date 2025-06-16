package com.movietheater.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.postgres.implicits._
import com.movietheater.domain.{Showtime, ShowtimeId, MovieId, TheaterId, Money}
import com.movietheater.db.DoobieInstances._
import java.time.LocalDateTime
import munit.CatsEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import doobie.Transactor
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.util.Properties

class ShowtimeRepositorySpec extends CatsEffectSuite {
  // Test database setup with TestContainers
  val postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:15"))
  postgres.start()

  val dbConfig = Map(
    "user" -> postgres.getUsername,
    "password" -> postgres.getPassword
  )

  val props = new Properties()
  dbConfig.foreach { case (k, v) => props.setProperty(k, v) }

  val transactor = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = postgres.getJdbcUrl,
    info = props,
    logHandler = None
  )

  // Initialize schema
  val initSchema = (for {
    _ <- sql"DROP TABLE IF EXISTS showtimes".update.run
    _ <- sql"DROP TABLE IF EXISTS seat_type_prices".update.run
    _ <- sql"DROP TABLE IF EXISTS seats".update.run
    _ <- sql"DROP TABLE IF EXISTS theaters".update.run
    _ <- sql"DROP TABLE IF EXISTS auditoriums".update.run
    _ <- sql"""
      CREATE TABLE theaters (
        id UUID PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        address VARCHAR(255) NOT NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL
      )
    """.update.run
    _ <- sql"""
      CREATE TABLE auditoriums (
        id UUID PRIMARY KEY,
        theater_id UUID NOT NULL REFERENCES theaters(id),
        name VARCHAR(255) NOT NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL
      )
    """.update.run
    _ <- sql"""
      CREATE TABLE seats (
        id VARCHAR(255) PRIMARY KEY,
        auditorium_id UUID NOT NULL REFERENCES auditoriums(id),
        row_number VARCHAR(255) NOT NULL,
        seat_number INTEGER NOT NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL
      )
    """.update.run
    _ <- sql"""
      CREATE TABLE showtimes (
        id UUID PRIMARY KEY,
        movie_id UUID NOT NULL REFERENCES movies(id),
        theater_id UUID NOT NULL REFERENCES theaters(id),
        auditorium_id UUID NOT NULL REFERENCES auditoriums(id),
        start_time TIMESTAMP NOT NULL,
        end_time TIMESTAMP NOT NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL
      )
    """.update.run
    _ <- sql"""
      CREATE TABLE showtime_seat_types (
        showtime_id UUID NOT NULL REFERENCES showtimes(id),
        seat_id VARCHAR(255) NOT NULL REFERENCES seats(id),
        seat_type VARCHAR(255) NOT NULL,
        PRIMARY KEY (showtime_id, seat_id)
      )
    """.update.run
    _ <- sql"""
      CREATE TABLE seat_type_prices (
        showtime_id UUID NOT NULL REFERENCES showtimes(id),
        seat_type VARCHAR(255) NOT NULL,
        price_cents BIGINT NOT NULL,
        PRIMARY KEY (showtime_id, seat_type)
      )
    """.update.run
    _ <- sql"""
      CREATE TABLE tickets (
        id UUID PRIMARY KEY,
        showtime_id UUID NOT NULL REFERENCES showtimes(id),
        seat_id VARCHAR(255) NOT NULL REFERENCES seats(id),
        customer_id UUID NOT NULL REFERENCES customers(id),
        price_cents BIGINT NOT NULL,
        status VARCHAR(255) NOT NULL,
        purchased_at TIMESTAMP NOT NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL
      )
    """.update.run
  } yield ()).transact(transactor).unsafeRunSync()

  override def beforeAll(): Unit = {
    initSchema
  }

  override def afterAll(): Unit = {
    postgres.stop()
  }

  // Test data generators
  implicit val showtimeIdArb: Arbitrary[ShowtimeId] = Arbitrary(Gen.uuid.map(ShowtimeId(_)))
  implicit val movieIdArb: Arbitrary[MovieId] = Arbitrary(Gen.uuid.map(MovieId(_)))
  implicit val theaterIdArb: Arbitrary[TheaterId] = Arbitrary(Gen.uuid.map(TheaterId(_)))
  implicit val localDateTimeArb: Arbitrary[LocalDateTime] = Arbitrary(
    Gen.choose(0L, 1000000000L).map(LocalDateTime.ofEpochSecond(_, 0, java.time.ZoneOffset.UTC))
  )

  val showtimeGen: Gen[Showtime] = for {
    id <- arbitrary[ShowtimeId]
    movieId <- arbitrary[MovieId]
    theaterId <- arbitrary[TheaterId]
    startTime <- arbitrary[LocalDateTime]
    endTime <- arbitrary[LocalDateTime].map(_.plusHours(2))
    price <- Gen.choose(500L, 5000L).map(Money(_)) // price in cents
  } yield Showtime(id, movieId, theaterId, startTime, endTime, price)

  // Repository instance
  val repository = new ShowtimeRepository(transactor)

  // Test cases
  test("create and findById") {
    for {
      showtime <- IO(showtimeGen.sample.get)
      created <- repository.create(showtime)
      found <- repository.findById(showtime.id)
    } yield {
      assertEquals(created, showtime)
      assertEquals(found, Some(showtime))
    }
  }

  test("findByMovieId") {
    for {
      movieId <- IO(MovieId(java.util.UUID.randomUUID()))
      showtime1 <- IO(showtimeGen.sample.get.copy(movieId = movieId))
      showtime2 <- IO(showtimeGen.sample.get.copy(movieId = movieId))
      _ <- repository.create(showtime1)
      _ <- repository.create(showtime2)
      found <- repository.findByMovieId(movieId)
    } yield {
      assertEquals(found.length, 2)
      assert(found.contains(showtime1))
      assert(found.contains(showtime2))
    }
  }

  test("findByTheaterId") {
    for {
      theaterId <- IO(TheaterId(java.util.UUID.randomUUID()))
      showtime1 <- IO(showtimeGen.sample.get.copy(theaterId = theaterId))
      showtime2 <- IO(showtimeGen.sample.get.copy(theaterId = theaterId))
      _ <- repository.create(showtime1)
      _ <- repository.create(showtime2)
      found <- repository.findByTheaterId(theaterId)
    } yield {
      assertEquals(found.length, 2)
      assert(found.contains(showtime1))
      assert(found.contains(showtime2))
    }
  }

  test("findByTimeRange") {
    for {
      baseTime <- IO(LocalDateTime.now())
      showtime1 <- IO(showtimeGen.sample.get.copy(
        startTime = baseTime.plusHours(1),
        endTime = baseTime.plusHours(3)
      ))
      showtime2 <- IO(showtimeGen.sample.get.copy(
        startTime = baseTime.plusHours(2),
        endTime = baseTime.plusHours(4)
      ))
      _ <- repository.create(showtime1)
      _ <- repository.create(showtime2)
      found <- repository.findByTimeRange(baseTime, baseTime.plusHours(5))
    } yield {
      assertEquals(found.length, 2)
      assert(found.contains(showtime1))
      assert(found.contains(showtime2))
    }
  }

  test("update") {
    for {
      showtime <- IO(showtimeGen.sample.get)
      _ <- repository.create(showtime)
      updatedShowtime = showtime.copy(price = Money(9999L))
      updated <- repository.update(updatedShowtime)
      found <- repository.findById(showtime.id)
    } yield {
      assertEquals(updated, updatedShowtime)
      assertEquals(found, Some(updatedShowtime))
    }
  }

  test("delete") {
    for {
      showtime <- IO(showtimeGen.sample.get)
      _ <- repository.create(showtime)
      _ <- repository.delete(showtime.id)
      found <- repository.findById(showtime.id)
    } yield {
      assertEquals(found, None)
    }
  }

  test("deleteAll") {
    for {
      showtime1 <- IO(showtimeGen.sample.get)
      showtime2 <- IO(showtimeGen.sample.get)
      _ <- repository.create(showtime1)
      _ <- repository.create(showtime2)
      _ <- repository.deleteAll
      found1 <- repository.findById(showtime1.id)
      found2 <- repository.findById(showtime2.id)
    } yield {
      assertEquals(found1, None)
      assertEquals(found2, None)
    }
  }
} 