-- V1__Create_initial_schema.sql

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create custom types
CREATE TYPE seat_type AS ENUM ('Regular', 'Premium', 'VIP');
CREATE TYPE ticket_status AS ENUM ('Reserved', 'Confirmed', 'Cancelled');
CREATE TYPE seat_status AS ENUM ('Available', 'Reserved', 'Booked');

-- Movies table
CREATE TABLE movies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    rating VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Theaters table
CREATE TABLE theaters (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    location VARCHAR(500) NOT NULL,
    capacity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Auditoriums table
CREATE TABLE auditoriums (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    theater_id UUID NOT NULL REFERENCES theaters(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seats table
CREATE TABLE seats (
    id VARCHAR(50) PRIMARY KEY,
    theater_id UUID NOT NULL REFERENCES theaters(id) ON DELETE CASCADE,
    auditorium_id UUID NOT NULL REFERENCES auditoriums(id) ON DELETE CASCADE,
    row_number VARCHAR(10) NOT NULL,
    seat_number INTEGER NOT NULL,
    seat_type seat_type NOT NULL DEFAULT 'Regular',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(theater_id, auditorium_id, row_number, seat_number)
);

-- Showtimes table
CREATE TABLE showtimes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    movie_id UUID NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    theater_id UUID NOT NULL REFERENCES theaters(id) ON DELETE CASCADE,
    auditorium_id UUID NOT NULL REFERENCES auditoriums(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seat type assignments table
CREATE TABLE seat_type_assignments (
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_id VARCHAR(50) NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    seat_type seat_type NOT NULL,
    PRIMARY KEY (showtime_id, seat_id)
);

-- Seat statuses table
CREATE TABLE seat_statuses (
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_id VARCHAR(50) NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    status seat_status NOT NULL DEFAULT 'Available',
    PRIMARY KEY (showtime_id, seat_id)
);

-- Seat type prices table
CREATE TABLE seat_type_prices (
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_type seat_type NOT NULL,
    price_cents BIGINT NOT NULL,
    PRIMARY KEY (showtime_id, seat_type)
);

-- Customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tickets table
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_id VARCHAR(50) NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    price_cents BIGINT NOT NULL,
    status ticket_status NOT NULL DEFAULT 'Reserved',
    reservation_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(showtime_id, seat_id)
);

-- Create indexes for better performance
CREATE INDEX idx_showtimes_movie_id ON showtimes(movie_id);
CREATE INDEX idx_showtimes_theater_id ON showtimes(theater_id);
CREATE INDEX idx_showtimes_auditorium_id ON showtimes(auditorium_id);
CREATE INDEX idx_showtimes_start_time ON showtimes(start_time);
CREATE INDEX idx_seats_theater_id ON seats(theater_id);
CREATE INDEX idx_seats_auditorium_id ON seats(auditorium_id);
CREATE INDEX idx_seat_type_assignments_showtime_id ON seat_type_assignments(showtime_id);
CREATE INDEX idx_seat_type_assignments_seat_id ON seat_type_assignments(seat_id);
CREATE INDEX idx_seat_statuses_showtime_id ON seat_statuses(showtime_id);
CREATE INDEX idx_seat_statuses_seat_id ON seat_statuses(seat_id);
CREATE INDEX idx_seat_type_prices_showtime_id ON seat_type_prices(showtime_id);
CREATE INDEX idx_tickets_showtime_id ON tickets(showtime_id);
CREATE INDEX idx_tickets_customer_id ON tickets(customer_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_customers_email ON customers(email); 