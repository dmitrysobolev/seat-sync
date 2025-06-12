# SeatSync - Movie Theater Ticket Reservation System

A comprehensive movie theater ticket reservation system built with Scala 3, Http4s, Cats Effect, and functional programming principles.

## Features

- **Movie Management**: CRUD operations for movies
- **Theater & Seat Management**: Theater and seat configuration with different seat types (Regular, Premium, VIP)
- **Showtime Scheduling**: Movie showtime management with pricing
- **Customer Management**: Customer registration and management
- **Ticket Reservations**: Complete ticket booking workflow with seat availability checking
- **RESTful API**: HTTP endpoints for all operations
- **Functional Architecture**: Clean separation of concerns with algebras and interpreters
- **Storage Options**: Support for both InMemory and PostgreSQL storage
- **Comprehensive Testing**: 149 tests with 97.32% statement coverage

## Architecture

The system follows Domain-Driven Design (DDD) and Clean Architecture principles:

```
src/main/scala/com/movietheater/
├── domain/          # Domain models and value objects
├── algebras/        # Abstract interfaces for business operations
├── interpreters/    # Concrete implementations (InMemory and Doobie)
├── services/        # Business logic and orchestration
├── http/           # HTTP routes and JSON codecs
├── config/         # Configuration management
└── db/             # Database utilities and migrations
```

## Technology Stack

- **Scala 3.7.1** - Modern functional programming language
- **Cats Effect 3** - Functional effect system and concurrency
- **Http4s** - Functional HTTP library
- **Circe** - JSON parsing and encoding
- **Doobie** - Functional database access layer
- **PostgreSQL** - Production database
- **Flyway** - Database migrations
- **Docker Compose** - Container orchestration
- **ScalaTest** - Testing framework
- **Scoverage** - Code coverage analysis

## Storage Options

SeatSync supports two storage backends:

### 1. InMemory Storage (Default)
- **Use case**: Development, testing, demonstrations
- **Pros**: No setup required, fast, simple
- **Cons**: Data lost on restart, not scalable
- **Start**: `sbt run`

### 2. PostgreSQL with Doobie
- **Use case**: Production, persistent storage
- **Pros**: ACID transactions, scalable, persistent
- **Cons**: Requires database setup
- **Start**: `USE_POSTGRES=true sbt run`

## Quick Start

### Prerequisites
- Java 17+
- SBT 1.8+
- Docker & Docker Compose (for PostgreSQL)

### 1. Clone and Build
```bash
git clone <repository-url>
cd tickets
sbt compile
```

### 2. Run with InMemory Storage
```bash
sbt run
```

### 3. Run with PostgreSQL
```bash
# Start PostgreSQL
./scripts/start-postgres.sh

# Run application with PostgreSQL
USE_POSTGRES=true sbt run
```

## PostgreSQL Setup

### Using Docker Compose

1. **Start PostgreSQL**:
   ```bash
   ./scripts/start-postgres.sh
   ```

2. **Database Details**:
   - Host: `localhost:5432`
   - Database: `seatsync`
   - User: `seatsync_user`
   - Password: `seatsync_password`

3. **pgAdmin Access** (optional):
   - URL: http://localhost:8080
   - Email: `admin@seatsync.com`
   - Password: `admin`

### Manual PostgreSQL Setup

If you prefer to use an existing PostgreSQL instance:

1. **Create Database**:
   ```sql
   CREATE DATABASE seatsync;
   CREATE USER seatsync_user WITH PASSWORD 'seatsync_password';
   GRANT ALL PRIVILEGES ON DATABASE seatsync TO seatsync_user;
   ```

2. **Update Configuration**:
   ```bash
   export DATABASE_URL="jdbc:postgresql://your-host:5432/seatsync"
   export DATABASE_USER="your_user"
   export DATABASE_PASSWORD="your_password"
   ```

## Configuration

Configuration is loaded from `src/main/resources/application.conf`:

```hocon
server {
  host = "0.0.0.0"
  host = ${?HOST}
  port = 8080
  port = ${?PORT}
}

database {
  driver = "org.postgresql.Driver"
  url = "jdbc:postgresql://localhost:5432/seatsync"
  url = ${?DATABASE_URL}
  user = "seatsync_user"
  user = ${?DATABASE_USER}
  password = "seatsync_password"
  password = ${?DATABASE_PASSWORD}
  pool-size = 10
  pool-size = ${?DATABASE_POOL_SIZE}
}
```

Environment variables override default values.

## API Endpoints

The API is available at `http://localhost:8080` with the following endpoints:

### Reservations
- `POST /reservations` - Create new reservation
- `PUT /reservations/:id/confirm` - Confirm reservation
- `DELETE /reservations/:id` - Cancel reservation

### Tickets
- `GET /customers/:customerId/tickets` - Get customer tickets

### Seats
- `GET /showtimes/:showtimeId/seats/available` - Get available seats

### Sample API Calls

```bash
# Get available seats for a showtime
curl http://localhost:8080/showtimes/123e4567-e89b-12d3-a456-426614174000/seats/available

# Create a reservation
curl -X POST http://localhost:8080/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "123e4567-e89b-12d3-a456-426614174000",
    "showtimeId": "123e4567-e89b-12d3-a456-426614174000",
    "seatIds": ["A1-1", "A1-2"]
  }'

# Confirm a reservation
curl -X PUT http://localhost:8080/reservations/ticket-id/confirm
```

## Database Schema

The PostgreSQL schema includes:

- **movies** - Movie information
- **theaters** - Theater venues
- **seats** - Individual seats with types (Regular, Premium, VIP)
- **showtimes** - Movie screening times and pricing
- **customers** - Customer information
- **tickets** - Reservation and booking records

Key features:
- UUID primary keys
- Foreign key constraints
- Enum types for seat types and ticket status
- Indexes for performance
- Timestamps for auditing

## Testing

### Run All Tests
```bash
sbt test
```

### Generate Coverage Report
```bash
sbt clean coverage test coverageReport
```

### View Coverage Report
```bash
# Open in browser
open target/scala-3.7.1/scoverage-report/index.html

# Or use the provided command
sbt run
```

### Test Statistics
- **149 total tests**
- **97.32% statement coverage**
- **100% branch coverage**

Test categories:
- Domain model tests
- Algebra implementation tests (InMemory and Doobie)
- Service logic tests
- HTTP endpoint tests
- Configuration tests

## Development

### Adding New Features

1. **Define Domain Models** in `domain/`
2. **Create Algebra Interface** in `algebras/`
3. **Implement Interpreters**:
   - InMemory version in `interpreters/`
   - Doobie version in `interpreters/`
4. **Add Business Logic** in `services/`
5. **Create HTTP Routes** in `http/routes/`
6. **Write Tests** for all layers

### Database Migrations

Database schema changes are managed with Flyway migrations in `src/main/resources/db/migration/`:

```
V1__Create_initial_schema.sql
V2__Add_new_feature.sql
```

Migrations run automatically on application startup when using PostgreSQL mode.

## Production Deployment

### Environment Variables
```bash
export USE_POSTGRES=true
export HOST=0.0.0.0
export PORT=8080
export DATABASE_URL=jdbc:postgresql://prod-db:5432/seatsync
export DATABASE_USER=prod_user
export DATABASE_PASSWORD=secure_password
export DATABASE_POOL_SIZE=20
```

### Docker Deployment
```dockerfile
# Example Dockerfile (not included)
FROM openjdk:17-jre-slim
COPY target/scala-3.7.1/seat-sync-assembly-*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is available under the MIT License. 