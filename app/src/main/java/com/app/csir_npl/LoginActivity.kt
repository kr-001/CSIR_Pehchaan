package com.app.csir_npl
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.csir_npl.R
import com.google.firebase.firestore.FirebaseFirestore
import com.app.csir_npl.User
class LoginActivity : AppCompatActivity() {
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextId = findViewById(R.id.editTextIdNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        firestore = FirebaseFirestore.getInstance()

        buttonLogin.setOnClickListener {
            val id = editTextId.text.toString()
            val password = editTextPassword.text.toString()

            // Authenticate user
            authenticateUser(id, password)
        }
    }

    private fun authenticateUser(id: String, password: String) {
        firestore.collection("users")
            .whereEqualTo("id", id)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val user = querySnapshot.documents[0].toObject(User::class.java)
                    if (user != null) {
                        displayIdCard(user)
                    } else {
                        Toast.makeText(this, "Failed to retrieve user details", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid ID number or password", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to authenticate: ${exception.message}", Toast.LENGTH_SHORT).show()
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

        // Create and add the user's DU Name
        val duNameText = TextView(this)
        duNameText.text = "DU Name: ${user.duName}"
        duNameText.textSize = 16f
        idCardLayout.addView(duNameText)

        // Create and add the user's DP Name
        val dpNameText = TextView(this)
        dpNameText.text = "DP Name: ${user.dpName}"
        dpNameText.textSize = 16f
        idCardLayout.addView(dpNameText)

        setContentView(idCardLayout)
    }

}
