package com.app.csir_npl
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.google.firebase.database.FirebaseDatabase
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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

    private val labNameLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedLabName =
                    data?.getStringExtra(LabNameActivity.EXTRA_SELECTED_LAB_NAME)
                if (selectedLabName != null) {
                    editTextLabName.setText(selectedLabName)
                    this.selectedLabName = selectedLabName
                }
            }
        }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                if (imageUri != null) {
                    selectedImageUri = imageUri

                    Glide.with(this)
                        .load(File(selectedImageUri.toString()))
                        .apply(
                            RequestOptions()
                                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                        )
                        .into(imageViewPhotoPreview)

                    photoPath = imageUri.toString()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

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

        val titleOptions = resources.getStringArray(R.array.title_options)
        val titleAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, titleOptions)
        spinnerTitle.adapter = titleAdapter

        editTextLabName.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val labNameInput = EditText(this)
                labNameInput.hint = "Enter Lab Name"

                AlertDialog.Builder(this)
                    .setTitle("Enter Lab Name")
                    .setView(labNameInput)
                    .setPositiveButton("OK") { dialog, _ ->
                        val enteredLabName = labNameInput.text.toString()
                        editTextLabName.setText(enteredLabName)
                        selectedLabName = enteredLabName
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }


        buttonUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

        buttonSubmit.setOnClickListener {
            if (validateInput()) {
                val user = User(
                    // ... (other user details)
                    spinnerTitle.selectedItem.toString(),
                    editTextFullName.text.toString(),
                    editTextDesignation.text.toString(),
                    editTextDivisionName.text.toString(),
                    selectedLabName,
                    editTextCityState.text.toString(),
                    editTextIDCardNumber.text.toString(),
                    photoPath, // Save the file path to Firebase
                    editTextPassword.text.toString()
                )

                // Save photo locally
                val photoFileName = "${selectedLabName}_${System.currentTimeMillis()}.jpg"
                val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), photoFileName)

                try {
                    val inputStream = contentResolver.openInputStream(selectedImageUri)
                    val outputStream = FileOutputStream(photoFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    user.photoPath = photoFile.absolutePath // Save the file path to the user object

                    // Save user to Firebase Realtime Database
                    saveUserToDatabase(user) // Pass the user object to the function
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@RegisterActivity, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun validateInput(): Boolean {
        val fullName = editTextFullName.text.toString().trim()
        val labName = editTextLabName.text.toString().trim()
        val password = editTextPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()

        if (fullName.isEmpty() || labName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        } else if (password != confirmPassword) {
            Toast.makeText(this, "Password and confirm password do not match", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }

    private fun saveUserToDatabase(user: User) {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val unverifiedUsersRef: DatabaseReference = database.getReference("unverified_users")

        unverifiedUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    unverifiedUsersRef.setValue(true)
                }

                val userKey = unverifiedUsersRef.push().key
                val userRef = unverifiedUsersRef.child(userKey!!)

                userRef.setValue(user)
                    .addOnSuccessListener {
                        Toast.makeText(this@RegisterActivity, "Registration successful", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@RegisterActivity, "Failed to check database", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
