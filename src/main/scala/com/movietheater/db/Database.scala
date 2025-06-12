package com.movietheater.db

import cats.effect._
import cats.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import com.movietheater.config.DatabaseConfig
import org.flywaydb.core.Flyway

object Database {

  def createTransactor[F[_]: Async](config: DatabaseConfig): Resource[F, Transactor[F]] = {
    for {
      // Create a fixed thread pool for the JDBC operations
      connectEC <- ExecutionContexts.fixedThreadPool[F](config.poolSize)
      
      // Create the HikariCP connection pool
      transactor <- HikariTransactor.newHikariTransactor[F](
        driverClassName = config.driver,
        url = config.url,
        user = config.user,
        pass = config.password,
        connectEC = connectEC
      )
    } yield transactor
  }

  def runMigrations[F[_]: Sync](config: DatabaseConfig): F[Unit] = {
    Sync[F].delay {
      val flyway = Flyway.configure()
        .dataSource(config.url, config.user, config.password)
        .locations("classpath:db/migration")
        .load()
      
      flyway.migrate()
    }.as(())
  }

  def createTestTransactor[F[_]: Async]: Resource[F, Transactor[F]] = {
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[F](4)
      transactor <- HikariTransactor.newHikariTransactor[F](
        driverClassName = "org.h2.Driver",
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "sa",
        pass = "",
        connectEC = connectEC
      )
    } yield transactor
  }
} 