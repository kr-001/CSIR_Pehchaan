package com.app.csir_npl

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private var connection: Connection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextId = findViewById(R.id.editTextIdNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Set up SQL Server connection
        setupConnection()

        buttonLogin.setOnClickListener {
            val id = editTextId.text.toString()
            val password = editTextPassword.text.toString()

            // Authenticate user
            authenticateUser(id, password)
        }
    }

    private fun setupConnection() {
        try {
            // Set up SQL Server connection parameters
            val connectionUrl = "jdbc:jtds:sqlserver://<your-instance-ip>:<port>/<database-name>"
            val username = "<username>"
            val password = "<password>"

            // Establish the connection
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connection = DriverManager.getConnection(connectionUrl, username, password)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect to SQL Server", Toast.LENGTH_SHORT).show()
        } catch (e: SQLException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect to SQL Server", Toast.LENGTH_SHORT).show()
        }
    }

    private fun authenticateUser(id: String, password: String) {
        try {
            val statement = connection!!.createStatement()
            val query = "SELECT * FROM users WHERE id = '$id' AND password = '$password'"
            val resultSet = statement.executeQuery(query)
            if (resultSet.next()) {
                val fullName = resultSet.getString("fullName")
                val designation = resultSet.getString("designation")
                val email = resultSet.getString("email")
                val mobile = resultSet.getString("mobile")
                val address = resultSet.getString("address")
                val cityState = resultSet.getString("cityState")
                val idCardNumber = resultSet.getString("idCardNumber")
                val photoPath = resultSet.getString("photoPath")
                val user = User(fullName, designation, email, mobile, address, cityState, idCardNumber, photoPath, password)
                displayIdCard(user)
            } else {
                Toast.makeText(this, "Invalid ID number or password", Toast.LENGTH_SHORT).show()
            }
            resultSet.close()
            statement.close()
        } catch (e: SQLException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to authenticate: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayIdCard(user: User) {
        val idCardLayout = LinearLayout(this)
        idCardLayout.orientation = LinearLayout.VERTICAL
        idCardLayout.setBackgroundColor(Color.WHITE)
        idCardLayout.setPadding(16, 16, 16, 16)

        // Create and add the user's profile picture
        val profilePicture = ImageView(this)
        // Set the profile picture image using user.getProfilePicture() or any other way you have implemented it
        // profilePicture.setImageBitmap(user.getProfilePicture());
        idCardLayout.addView(profilePicture)

        // Create and add the user's full name
        val fullNameText = TextView(this)
        fullNameText.text = "Full Name: " + user.fullName
        fullNameText.textSize = 20f
        fullNameText.setTypeface(null, Typeface.BOLD)
        idCardLayout.addView(fullNameText)

        // Create and add the user's designation
        val designationText = TextView(this)
        designationText.text = "Designation: " + user.designation
        designationText.textSize = 16f
        idCardLayout.addView(designationText)

        // Create and add the user's division name
        val divisionNameText = TextView(this)
        divisionNameText.text = "Division Name: " + user.divisionName
        divisionNameText.textSize = 16f
        idCardLayout.addView(divisionNameText)

        // Create and add the user's lab name
        val labNameText = TextView(this)
        labNameText.text = "Lab Name: " + user.labName
        labNameText.textSize = 16f
        idCardLayout.addView(labNameText)

        // Create and add the user's city/state
        val cityStateText = TextView(this)
        cityStateText.text = "City/State: " + user.cityState
        cityStateText.textSize = 16f
        idCardLayout.addView(cityStateText)

        // Display the ID card layout
        setContentView(idCardLayout)
    }

}
