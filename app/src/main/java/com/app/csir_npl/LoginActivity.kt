package com.app.csir_npl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextId = findViewById(R.id.editTextIdNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        // Set up Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("unverified_users")

        buttonLogin.setOnClickListener {
            val id = editTextId.text.toString()
            val password = editTextPassword.text.toString()

            // Authenticate user
            authenticateUser(id, password)
        }
    }

    private fun authenticateUser(id: String, password: String) {
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isValid = false
                var currentUser: User? = null
                for (userSnapshot in dataSnapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.idCardNumber == id && user.password == password) {
                        isValid = true
                        currentUser = user
                        break
                    }
                }

                if (isValid && currentUser != null) {
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                    // Pass user data to the ID card activity
                    val intent = Intent(this@LoginActivity, IdCardActivity::class.java)
                    intent.putExtra("user", currentUser)
                    startActivity(intent)

                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid ID number or password", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Failed to authenticate: " + databaseError.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
