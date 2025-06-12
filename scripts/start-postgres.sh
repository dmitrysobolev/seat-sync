#!/bin/bash

# Script to start PostgreSQL using Docker Compose

echo "Starting PostgreSQL database..."
docker compose up -d postgres

echo "Waiting for PostgreSQL to be ready..."
sleep 10

echo "Checking PostgreSQL health..."
docker compose exec postgres pg_isready -U seatsync_user -d seatsync

if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL is ready!"
    echo ""
    echo "Database connection details:"
    echo "  Host: localhost"
    echo "  Port: 5432"
    echo "  Database: seatsync"
    echo "  User: seatsync_user"
    echo "  Password: seatsync_password"
    echo ""
    echo "To run the application with PostgreSQL:"
    echo "  USE_POSTGRES=true sbt run"
    echo ""
    echo "To run with InMemory storage:"
    echo "  sbt run"
    echo ""
    echo "To access pgAdmin:"
    echo "  http://localhost:8080"
    echo "  Email: admin@seatsync.com"
    echo "  Password: admin"
else
    echo "❌ PostgreSQL is not ready. Check Docker logs:"
    echo "  docker compose logs postgres"
fi 