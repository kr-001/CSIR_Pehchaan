package com.app.csir_npl
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
class UserDatabaseHelper(private val dbHelper: DatabaseHelper) {

    private val tableName = "csir_users"

    fun saveUserToDatabase(user: User) {
        dbHelper.initialize()

        val connection = dbHelper.getConnection()

        // Create the table if it doesn't exist
        createTableIfNotExists(connection)

        val sql =
            "INSERT INTO $tableName (title, full_name, designation, division_name, lab_name, city_state, id_card_number, photo_path, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"

        try {
            val preparedStatement: PreparedStatement =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            preparedStatement.setString(1, user.title)
            preparedStatement.setString(2, user.fullName)
            preparedStatement.setString(3, user.designation)
            preparedStatement.setString(4, user.divisionName)
            preparedStatement.setString(5, user.labName)
            preparedStatement.setString(6, user.cityState)
            preparedStatement.setString(7, user.idCardNumber)
            preparedStatement.setString(8, user.photoPath)
            preparedStatement.setString(9, user.password)

            val affectedRows = preparedStatement.executeUpdate()
            if (affectedRows > 0) {
                // Registration successful
            } else {
                // Registration failed
            }

            preparedStatement.close()
            dbHelper.closeConnection()
        } catch (e: SQLException) {
            e.printStackTrace()
            // Registration failed
        }
    }

    private fun createTableIfNotExists(connection: Connection) {
        val sql = "IF OBJECT_ID('$tableName', 'U') IS NULL " +
                "CREATE TABLE $tableName (" +
                "id INT IDENTITY(1,1) PRIMARY KEY, " +
                "title VARCHAR(10), " +
                "full_name VARCHAR(100), " +
                "designation VARCHAR(100), " +
                "division_name VARCHAR(100), " +
                "lab_name VARCHAR(100), " +
                "city_state VARCHAR(100), " +
                "id_card_number VARCHAR(20), " +
                "photo_path VARCHAR(255), " +
                "password VARCHAR(100)" +
                ")"

        try {
            val statement = connection.createStatement()
            statement.executeUpdate(sql)
            statement.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}

