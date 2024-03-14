package io.github.ipaddicting.capsule

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import java.sql.Connection

/**
 * The JDBC connection manager for TDengine.
 */
@Singleton
class ConnectionManager {
    private val jdbcUrl = System.getProperty("tdengine.jdbc.url", "jdbc:TAOS-RS://localhost:6041/power")
    private val username = System.getProperty("tdengine.jdbc.user", "root")
    private val password = System.getProperty("tdengine.jdbc.password", "taosdata")
    private val maxSize = System.getProperty("tdengine.jdbc.max-size", "1").toInt()
    private val minSize = System.getProperty("tdengine.jdbc.min-size", "1").toInt()
    private val connectionTimeout = System.getProperty("tdengine.jdbc.connection-timeout", "30000").toLong()

    private lateinit var datasource: HikariDataSource

    /**
     * Initialize the connection pool.
     */
    @PostConstruct
    fun init() {
        val config = HikariConfig()

        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.maximumPoolSize = maxSize
        config.minimumIdle = minSize
        config.connectionTimeout = connectionTimeout
        config.connectionTestQuery = "select server_status()"

        datasource = HikariDataSource(config)
    }

    /**
     * Retrieve a database connection of TDengine.
     */
    fun get(): Connection = datasource.connection

    /**
     * Close the connection pool.
     */
    @PreDestroy
    fun del() {
        datasource.close()
    }
}
