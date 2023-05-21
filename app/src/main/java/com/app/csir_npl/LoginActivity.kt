package com.app.csir_npl

import DatabaseHelper
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextId = findViewById(R.id.editTextIdNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        databaseHelper = DatabaseHelper()

        buttonLogin.setOnClickListener {
            val id = editTextId.text.toString()
            val password = editTextPassword.text.toString()

            // Authenticate user
            val isAuthenticated = databaseHelper.authenticateUser(id, password)
            if (isAuthenticated) {
                val user = databaseHelper.getUserDetails(id , password)
                if (user != null) {
                    displayIdCard(user)
                } else {
                    Toast.makeText(this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Invalid ID number or password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayIdCard(user: DatabaseHelper.User) {
        val idCardLayout = LinearLayout(this)
        idCardLayout.orientation = LinearLayout.VERTICAL
        idCardLayout.setBackgroundColor(Color.WHITE)
        idCardLayout.setPadding(16, 16, 16, 16)

        // Create and add the user's profile picture
        val profilePicture = ImageView(this)
        // Set the profile picture image using user.getProfilePicture() or any other way you have implemented it
        // profilePicture.setImageBitmap(user.getProfilePicture())
        idCardLayout.addView(profilePicture)

        // Create and add the user's full name
        val fullNameText = TextView(this)
        fullNameText.text = "Full Name: ${user.fullName}"
        fullNameText.textSize = 20f
        fullNameText.setTypeface(null, Typeface.BOLD)
        idCardLayout.addView(fullNameText)

        // Create and add the user's designation
        val designationText = TextView(this)
        designationText.text = "Designation: ${user.designation}"
        designationText.textSize = 16f
        idCardLayout.addView(designationText)

        // Create and add the user's email
        val emailText = TextView(this)
        emailText.text = "Email: ${user.email}"
        emailText.textSize = 16f
        idCardLayout.addView(emailText)

        // Create and add the user's mobile number
        val mobileText = TextView(this)
        mobileText.text = "Mobile: ${user.mobile}"
        mobileText.textSize = 16f
        idCardLayout.addView(mobileText)

        // Create and add the user's address
        val addressText = TextView(this)
        addressText.text = "Address: ${user.address}"
        addressText.textSize = 16f
        idCardLayout.addView(addressText)

        setContentView(idCardLayout)
    }
}
