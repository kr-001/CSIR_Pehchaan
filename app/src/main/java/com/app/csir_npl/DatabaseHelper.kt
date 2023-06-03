import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class DatabaseHelper {
    private lateinit var dataSource: HikariDataSource

    fun initialize() {
        val config = HikariConfig().apply {
            // Set your database connection details
            jdbcUrl = "jdbc:mysql://localhost:3306/csir_pehchaan"
            username = "root"
            password = "Ravi@1998"
            driverClassName = "com.mysql.cj.jdbc.Driver"
        }

        dataSource = HikariDataSource(config)
    }

    fun getConnection(): Connection {
        return dataSource.connection
    }

    fun close() {
        dataSource.close()
    }
}
