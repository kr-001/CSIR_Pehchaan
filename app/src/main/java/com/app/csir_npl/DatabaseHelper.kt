package com.app.csir_npl
import android.widget.Toast
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.PreparedStatement

class DatabaseHelper {
    private lateinit var dataSource: HikariDataSource

    fun initialize() {
        val config = HikariConfig().apply {
            // Set your database connection details
            jdbcUrl = "jdbc:sqlserver://35.232.226.19:1433;DatabaseName=csir_db;encrypt=true;trustServerCertificate=true;"
            username = "sqlserver"
            password = "sql123"
            driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
        }

        dataSource = HikariDataSource(config)
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    fun closeConnection() {
        dataSource.close()
    }


}
