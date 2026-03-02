package com.dandd.templateparser.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Database")

fun Application.configureDatabase() {
    val dbConfig = environment.config.config("database")
    val poolConfig = dbConfig.config("pool")

    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = dbConfig.property("url").getString()
            username = dbConfig.property("user").getString()
            password = dbConfig.property("password").getString()
            maximumPoolSize = poolConfig.property("maximum-pool-size").getString().toInt()
            minimumIdle = poolConfig.property("minimum-idle").getString().toInt()
            connectionTimeout = poolConfig.property("connection-timeout-ms").getString().toLong()
            driverClassName = "com.mysql.cj.jdbc.Driver"
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }

    val dataSource = HikariDataSource(hikariConfig)

    Flyway
        .configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()
        .also { result ->
            logger.info("Flyway applied {} migration(s)", result.migrationsExecuted)
        }

    Database.connect(dataSource)
    logger.info("Database connected successfully")
}
