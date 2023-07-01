package com.app.csir_npl

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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
                if (imageUri != null && fileExists(imageUri)) {
                    selectedImageUri = imageUri

                    Glide.with(this)
                        .load(selectedImageUri)
                        .apply(
                            RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                        )
                        .into(imageViewPhotoPreview)

                    photoPath = getFilePathFromUri(selectedImageUri) ?: ""
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "File not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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
                Log.i("RegisterActivity", "User data: $user")

                // Upload user data to the server
                uploadUserData(user, photoPath)
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
                    Toast.makeText(
                        this,
                        "Password and confirm password do not match",
                        Toast.LENGTH_SHORT
                    ).show()
                    return false
                }

                return true
            }

    private fun uploadUserData(user: User, photoPath: String) {
        val client = OkHttpClient()

        // Create RequestBody for sending form data
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", user.title)
            .addFormDataPart("fullName", user.fullName)
            .addFormDataPart("designation", user.designation)
            .addFormDataPart("divisionName", user.divisionName)
            .addFormDataPart("labName", user.labName)
            .addFormDataPart("cityState", user.cityState)
            .addFormDataPart("idCardNumber", user.idCardNumber)
            .addFormDataPart("password", user.password)
            .addFormDataPart("photoPath", photoPath)
            .build()

        // Create POST request
        val request = Request.Builder()
            .url("http://192.168.0.222:3000/register") // Replace <YOUR_SERVER_HOST> with your server's IP address or domain name
            .post(requestBody)
            .build()

        // Send the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Failed to upload user data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseMessage = response.message
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration Successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed: $responseMessage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }



    private fun fileExists(uri: Uri): Boolean {
        val filePath = getFilePathFromUri(uri)
        if (filePath != null) {
            val file = File(filePath)
            return file.exists()
        }
        return false
    }


    private fun getFilePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val filePath = cursor.getString(columnIndex)
            cursor.close()
            return filePath
        }
        return null
    }
}