# SeatSync

A functional Scala movie theater ticket reservation system built with **Tagless Final** pattern and **Cats Effect**.

## Architecture

SeatSync demonstrates a clean architecture using the Tagless Final pattern, which provides:

- **Modularity**: Clear separation between algebra definitions and their implementations
- **Testability**: Easy to mock dependencies and test business logic
- **Flexibility**: Can swap implementations (in-memory, database, etc.) without changing business logic
- **Type Safety**: Leverages Scala's type system for compile-time guarantees

### Key Components

#### Domain Models (`com.movietheater.domain`)
- Core entities: `Movie`, `Theater`, `Seat`, `Showtime`, `Ticket`, `Customer`
- Value classes for type safety: `MovieId`, `TheaterId`, etc.
- Algebraic data types for states: `SeatType`, `TicketStatus`
- Domain errors with meaningful messages

#### Algebras (`com.movietheater.algebras`)
- **Tagless Final** interfaces defining operations
- `MovieAlgebra[F[_]]`, `TheaterAlgebra[F[_]]`, `SeatAlgebra[F[_]]`, etc.
- Effect-polymorphic (works with any effect type `F`)

#### Interpreters (`com.movietheater.interpreters`)
- Concrete implementations of algebras
- In-memory implementations for development/testing
- Easy to add database-backed implementations

#### Services (`com.movietheater.services`)
- `ReservationService`: Orchestrates business logic
- Uses algebras through dependency injection
- Handles validation and error scenarios

#### HTTP Layer (`com.movietheater.http`)
- RESTful API built with **Http4s**
- JSON serialization with **Circe**
- Error handling with proper HTTP status codes

## Features

### Core Functionality
- ✅ View available seats for showtimes
- ✅ Create reservations
- ✅ Confirm reservations (convert to purchases)
- ✅ Cancel reservations
- ✅ View customer ticket history
- ✅ Tiered pricing (Regular, Premium, VIP seats)

### Technical Features
- ✅ **Tagless Final** pattern for modularity
- ✅ **Cats Effect** for functional effects
- ✅ Type-safe domain modeling
- ✅ JSON API with automatic serialization
- ✅ Comprehensive error handling
- ✅ Unit tests demonstrating pattern usage

## API Endpoints

### Get Available Seats
```http
GET /showtimes/{showtimeId}/seats
```

### Create Reservation
```http
POST /reservations
Content-Type: application/json

{
  "showtimeId": "uuid",
  "seatIds": ["A1-1", "A1-2"],
  "customerId": "uuid"
}
```

### Confirm Reservation
```http
POST /reservations/confirm
Content-Type: application/json

["ticketId1", "ticketId2"]
```

### Cancel Reservation
```http
POST /reservations/cancel
Content-Type: application/json

["ticketId1", "ticketId2"]
```

### Get Customer Tickets
```http
GET /customers/{customerId}/tickets
```

## Running the Application

### Prerequisites
- Java 11+
- SBT 1.9+

### Start the Server
```bash
sbt run
```

The server will start on `http://localhost:8080`

### Run Tests
```bash
sbt test
```

## Project Structure

```
src/
├── main/scala/com/movietheater/
│   ├── Main.scala                    # Application entry point
│   ├── domain/
│   │   └── Models.scala              # Domain models and errors
│   ├── algebras/                     # Tagless Final interfaces
│   │   ├── MovieAlgebra.scala
│   │   ├── TheaterAlgebra.scala
│   │   ├── SeatAlgebra.scala
│   │   ├── ShowtimeAlgebra.scala
│   │   ├── TicketAlgebra.scala
│   │   └── CustomerAlgebra.scala
│   ├── interpreters/                 # Algebra implementations
│   │   ├── InMemoryMovieAlgebra.scala
│   │   ├── InMemoryTheaterAlgebra.scala
│   │   ├── InMemorySeatAlgebra.scala
│   │   ├── InMemoryShowtimeAlgebra.scala
│   │   ├── InMemoryTicketAlgebra.scala
│   │   └── InMemoryCustomerAlgebra.scala
│   ├── services/
│   │   └── ReservationService.scala  # Business logic orchestration
│   ├── http/
│   │   ├── json/
│   │   │   └── JsonCodecs.scala      # JSON serialization
│   │   └── routes/
│   │       └── ReservationRoutes.scala # HTTP endpoints
│   └── config/
│       └── AppConfig.scala           # Configuration classes
└── test/scala/com/movietheater/
    └── services/
        └── ReservationServiceSpec.scala # Service tests
```

## Tagless Final Pattern Benefits

### 1. **Modularity**
```scala
// Define what you can do, not how
trait MovieAlgebra[F[_]] {
  def findById(id: MovieId): F[Option[Movie]]
  def create(movie: Movie): F[Movie]
}

// Multiple implementations possible
class InMemoryMovieAlgebra[F[_]: Sync] extends MovieAlgebra[F]
class DatabaseMovieAlgebra[F[_]: Async] extends MovieAlgebra[F]
```

### 2. **Testability**
```scala
// Easy to create test implementations
val mockMovieAlgebra = new MovieAlgebra[IO] {
  def findById(id: MovieId) = IO.pure(Some(testMovie))
  def create(movie: Movie) = IO.pure(movie)
}
```

### 3. **Effect Polymorphism**
```scala
// Works with any effect type F[_]
class ReservationService[F[_]: MonadThrow](
  movieAlgebra: MovieAlgebra[F],
  // ... other algebras
)
```

## Dependencies

- **Cats Effect** - Functional effects and concurrency
- **Http4s** - HTTP server and client library
- **Circe** - JSON library for Scala
- **ScalaTest** - Testing framework

## Future Enhancements

### Database Integration
Replace in-memory interpreters with database-backed implementations:
- **Doobie** for database access
- **Flyway** for migrations
- Connection pooling

### Advanced Features
- Payment processing integration
- Email notifications
- Seat selection timeout
- Concurrency control for seat booking
- Analytics and reporting

### Infrastructure
- **Docker** containerization
- **Kubernetes** deployment
- Monitoring and logging
- CI/CD pipeline

## Learning Resources

- [Cats Effect Documentation](https://typelevel.org/cats-effect/)
- [Tagless Final Pattern](https://typelevel.org/blog/2017/12/27/optimizing-final-tagless.html)
- [Http4s Documentation](https://http4s.org/)
- [Circe Documentation](https://circe.github.io/circe/)

---

**SeatSync** demonstrates a production-ready approach to building functional Scala applications with the Tagless Final pattern and Cats Effect. You can easily extend it with database persistence, additional features, or different effect types while maintaining the same clean architecture! 