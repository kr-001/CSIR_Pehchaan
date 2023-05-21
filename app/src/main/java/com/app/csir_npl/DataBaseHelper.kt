import java.io.File
import java.io.FileOutputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

class DatabaseHelper {
    private val dbHost = "localhost"
    private val dbPort = 3306
    private val dbName = "csir_npl"
    private val dbUser = "root"
    private val dbPassword = "Ravi@1998"

    data class User(
        val fullName: String,
        val designation: String,
        val email: String,
        val mobile: String,
        val address: String,
        val filePath: String
    )

    fun insertUserDetails(
        fullName: String,
        designation: String,
        email: String,
        mobile: String,
        address: String,
        password: String,
        imageFile: File
    ) {
        val query =
            "INSERT INTO users (full_name, designation, email, mobile, address, password, file_path) VALUES (?, ?, ?, ?, ?, ?, ?)"

        // Open a connection to the database
        DriverManager.getConnection(getUrl(), dbUser, dbPassword).use { connection ->
            // Check if the users table exists
            val tableExistsQuery = "SHOW TABLES LIKE 'users'"
            val tableExists = connection.prepareStatement(tableExistsQuery).use { statement ->
                statement.executeQuery().next()
            }

            // If the users table does not exist, create it
            if (!tableExists) {
                val createTableQuery = "CREATE TABLE users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "full_name VARCHAR(255) NOT NULL," +
                        "designation VARCHAR(255) NOT NULL," +
                        "email VARCHAR(255) NOT NULL," +
                        "mobile VARCHAR(255) NOT NULL," +
                        "address VARCHAR(255) NOT NULL," +
                        "password VARCHAR(255) NOT NULL," +
                        "file_path VARCHAR(255) NOT NULL" +
                        ")"
                connection.prepareStatement(createTableQuery).use { statement ->
                    statement.executeUpdate()
                }
            }

            // Save the image file
            val uniqueFileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.${getFileExtension(imageFile)}"
            val destinationFile = File("path/to/your/images/directory", uniqueFileName)
            imageFile.copyTo(destinationFile)

            // Insert user details into the users table
            connection.prepareStatement(query).use { statement ->
                // Set the parameter values
                statement.setString(1, fullName)
                statement.setString(2, designation)
                statement.setString(3, email)
                statement.setString(4, mobile)
                statement.setString(5, address)
                statement.setString(6, password)
                statement.setString(7, destinationFile.absolutePath)

                // Execute the query
                statement.executeUpdate()
            }
        }
    }

    fun authenticateUser(id: String, password: String): Boolean {
        val query = "SELECT * FROM users WHERE id = ? AND password = ?"

        // Open a connection to the database
        DriverManager.getConnection(getUrl(), dbUser, dbPassword).use { connection ->
            connection.prepareStatement(query).use { statement ->
                // Set the parameter values
                statement.setString(1, id)
                statement.setString(2, password)

                // Execute the query
                val resultSet: ResultSet = statement.executeQuery()

                // Check if a user was found
                if (resultSet.next()) {
                    return true
                }
            }
        }

        return false
    }

    fun getUserDetails(email: String, password: String): User? {
        val query = "SELECT * FROM users WHERE email = ? AND password = ?"

        // Open a connection to the database
        DriverManager.getConnection(getUrl(), dbUser, dbPassword).use { connection ->
            connection.prepareStatement(query).use { statement ->
                // Set the parameter values
                statement.setString(1, email)
                statement.setString(2, password)

                // Execute the query
                val resultSet: ResultSet = statement.executeQuery()

                // Check if a user was found
                if (resultSet.next()) {
                    val fullName = resultSet.getString("full_name")
                    val designation = resultSet.getString("designation")
                    val mobile = resultSet.getString("mobile")
                    val address = resultSet.getString("address")
                    val filePath = resultSet.getString("file_path")

                    return User(fullName, designation, email, mobile, address, filePath)
                }
            }
        }

        return null
    }

    private fun getUrl(): String {
        return "jdbc:mysql://$dbHost:$dbPort/$dbName"
    }

    private fun getFileExtension(file: File): String {
        val fileName = file.name
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex != -1) fileName.substring(dotIndex + 1) else ""
    }
}
