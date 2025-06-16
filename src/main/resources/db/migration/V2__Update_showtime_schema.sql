-- V2__Update_showtime_schema.sql

-- Create seat status enum type
CREATE TYPE seat_status AS ENUM ('Available', 'Reserved', 'Booked');

-- Create seat type assignments table
CREATE TABLE seat_type_assignments (
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_id VARCHAR(50) NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    seat_type seat_type NOT NULL,
    PRIMARY KEY (showtime_id, seat_id)
);

-- Create seat statuses table
CREATE TABLE seat_statuses (
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_id VARCHAR(50) NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    status seat_status NOT NULL DEFAULT 'Available',
    PRIMARY KEY (showtime_id, seat_id)
);

-- Create seat type prices table
CREATE TABLE seat_type_prices (
    showtime_id UUID NOT NULL REFERENCES showtimes(id) ON DELETE CASCADE,
    seat_type seat_type NOT NULL,
    price_cents BIGINT NOT NULL,
    PRIMARY KEY (showtime_id, seat_type)
);

-- Add auditorium_id to showtimes
ALTER TABLE showtimes ADD COLUMN auditorium_id UUID NOT NULL REFERENCES auditoriums(id) ON DELETE CASCADE;

-- Remove old price column from showtimes
ALTER TABLE showtimes DROP COLUMN price;

-- Create indexes for better performance
CREATE INDEX idx_seat_type_assignments_showtime_id ON seat_type_assignments(showtime_id);
CREATE INDEX idx_seat_type_assignments_seat_id ON seat_type_assignments(seat_id);
CREATE INDEX idx_seat_statuses_showtime_id ON seat_statuses(showtime_id);
CREATE INDEX idx_seat_statuses_seat_id ON seat_statuses(seat_id);
CREATE INDEX idx_seat_type_prices_showtime_id ON seat_type_prices(showtime_id); 