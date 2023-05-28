package com.app.csir_npl
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement


class RegisterActivity : AppCompatActivity() {

    private lateinit var spinnerTitle: Spinner
    private lateinit var editTextFullName: EditText
    private lateinit var editTextDesignation: EditText
    private lateinit var editTextDivisionName: EditText
    private lateinit var editTextLabName: EditText
    private lateinit var editTextCityState: EditText
    private lateinit var editTextIDCardNumber: EditText
    private lateinit var buttonUploadPhoto: Button
    private lateinit var imageViewPhotoPreview: ImageView
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedLabName: String

    private var photoPath: String = ""

    private lateinit var labNameLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        spinnerTitle = findViewById(R.id.spinnerTitle)
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextDesignation = findViewById(R.id.editTextDesignation)
        editTextDivisionName = findViewById(R.id.editTextDivisionName)
        editTextLabName = findViewById(R.id.editTextLabName)
        editTextCityState = findViewById(R.id.editTextCityState)
        editTextIDCardNumber = findViewById(R.id.editTextIDCardNumber)
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto)
        imageViewPhotoPreview = findViewById(R.id.imageViewPhotoPreview)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)

        buttonSubmit = findViewById(R.id.buttonSubmit)

        // Set up the spinner for title selection
        val titleOptions = resources.getStringArray(R.array.title_options)
        val titleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, titleOptions)
        spinnerTitle.adapter = titleAdapter

        // Set up lab name autofill when clicked
        labNameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // Retrieve the selected lab name from the LabNameActivity
                val selectedLabName = data?.getStringExtra(LabNameActivity.EXTRA_SELECTED_LAB_NAME)
                if (selectedLabName != null) {
                    // Set the selected lab name in the editTextLabName field
                    editTextLabName.setText(selectedLabName)
                    this.selectedLabName = selectedLabName
                }
            }
        }

        editTextLabName.setOnClickListener {
            // Start a new activity to select a lab name
            val intent = Intent(this, LabNameActivity::class.java)
            labNameLauncher.launch(intent)
        }


        // Set up photo upload button
        buttonUploadPhoto.setOnClickListener {
            // Open an image picker to select a photo
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

// After selecting an image in onActivityResult or the ActivityResultLauncher callback
// Load the selected image into the imageViewPhotoPreview using Glide
        Glide.with(this)
            .load(File(selectedImageUri.toString()))
            .apply(
                RequestOptions()
                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
            )
            .into(imageViewPhotoPreview)
        // Set up submit button click listener
        buttonSubmit.setOnClickListener {
            // Validate user input
            if (validateInput()) {
                // Create an instance of the User class and populate the data
                val user = User(
                    spinnerTitle.selectedItem.toString(),
                    editTextFullName.text.toString(),
                    editTextDesignation.text.toString(),
                    editTextDivisionName.text.toString(),
                    selectedLabName,
                    editTextCityState.text.toString(),
                    editTextIDCardNumber.text.toString(),
                    photoPath,
                    editTextPassword.text.toString()
                )

                // Save user data to the database
                saveUserToDatabase(user)

                // Show a success message or perform any other action
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                // Finish the activity
                finish()
            }
        }
    }

    private fun validateInput(): Boolean {
        // Perform input validation here
        // You can add your own validation rules based on your requirements

        val fullName = editTextFullName.text.toString().trim()
        val labName = editTextLabName.text.toString().trim()
        val password = editTextPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()

        // Example validation rules: All fields must be non-empty and password and confirm password must match
        if (fullName.isEmpty() || labName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        } else if (password != confirmPassword) {
            Toast.makeText(this, "Password and confirm password do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        // Additional validation rules can be added here

        return true
    }

    private fun saveUserToDatabase(user: User) {
        // Perform database operation to save the user data
        // You will need to implement your own logic based on your database setup

        // Database connection details
        val url = "jdbc:sqlserver://plasma-moment-388114:us-central1:ravikumar;databaseName=csir_pehchaan"
        val username = "ravikumar"
        val password = "Ravi@1998"

        var connection: Connection? = null
        var statement: Statement? = null

        try {
            // Register the JDBC driver (if not already done)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")

            // Establish the database connection
            connection = DriverManager.getConnection(url, username, password)

            // Create the statement
            statement = connection.createStatement()

            // Create the INSERT query
            val query = """
    CREATE TABLE IF NOT EXISTS unverified_users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT,
        full_name TEXT,
        designation TEXT,
        division_name TEXT,
        lab_name TEXT,
        city_state TEXT,
        id_card_number TEXT,
        photo_path TEXT,
        password TEXT
    );

    INSERT INTO unverified_users (title, full_name, designation, division_name, lab_name, city_state, id_card_number, photo_path, password)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
"""

            val preparedStatement = connection!!.prepareStatement(query)
            preparedStatement.setString(1, user.title)
            preparedStatement.setString(2, user.fullName)
            preparedStatement.setString(3, user.designation)
            preparedStatement.setString(4, user.divisionName)
            preparedStatement.setString(5, user.labName)
            preparedStatement.setString(6, user.cityState)
            preparedStatement.setString(7, user.idCardNumber)
            preparedStatement.setString(8, user.photoPath)
            preparedStatement.setString(9, user.password)

            preparedStatement.executeUpdate()
            preparedStatement.close()

            // Execute the query
            statement.executeUpdate(query)

            // Query executed successfully

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            // Close the statement and connection
            statement?.close()
            connection?.close()
        }
    }
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            if (imageUri != null) {
                selectedImageUri = imageUri

                // Load the selected image into imageViewPhotoPreview using Glide
                Glide.with(this)
                    .load(selectedImageUri)
                    .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                    .into(imageViewPhotoPreview)

                // Store the photo path or URL
                photoPath = imageUri.toString()
            }
        }
    }
}
