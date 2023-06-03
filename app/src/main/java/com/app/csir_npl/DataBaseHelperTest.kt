import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class DatabaseHelperTest {

    companion object {
        private lateinit var databaseHelper: DatabaseHelper

        @BeforeClass
        @JvmStatic
        fun setup() {
            databaseHelper = DatabaseHelper()
            databaseHelper.initialize()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            databaseHelper.close()
        }
    }

    @Test
    fun testConnection() {
        val connection: Connection = databaseHelper.getConnection()
        assertEquals(true, connection.isValid(5)) // Check if the connection is valid within 5 seconds
        connection.close()
    }

    @Test
    fun testQueryExecution() {
        val connection: Connection = databaseHelper.getConnection()

        val statement: Statement = connection.createStatement()
        val query = "SELECT COUNT(*) as total FROM csir_institutes"
        val resultSet: ResultSet = statement.executeQuery(query)
        resultSet.next()
        val totalCount: Int = resultSet.getInt("total")

        resultSet.close()
        statement.close()
        connection.close()

        assertEquals(38, totalCount) // Replace 10 with the expected result count
    }
}
