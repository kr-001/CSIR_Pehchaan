package com.app.csir_npl

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
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
import java.io.InputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var spinnerTitle: Spinner
    private lateinit var editTextFullName: EditText
    private lateinit var editTextDesignation: EditText
    private lateinit var editTextDivisionName: EditText
    private lateinit var spinnerLabName: Spinner
    private lateinit var editTextCityState: EditText
    private lateinit var editTextIDCardNumber: EditText
    private lateinit var buttonUploadPhoto: Button
    private lateinit var imageViewPhotoPreview: ImageView
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var selectedImageUri: Uri
    private lateinit var selectedLabName: String
    private lateinit var editTextEmail: EditText
    private lateinit var editTextContactNumber: EditText


    private var photoPath: String = ""

    private val labNameLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedLabName =
                    data?.getStringExtra(LabNameActivity.EXTRA_SELECTED_LAB_NAME)
                if (selectedLabName != null) {
                    spinnerLabName.setSelection(getLabNameIndex(selectedLabName))
                    this.selectedLabName = selectedLabName
                }
            }
        }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data
            if (imageUri != null) {
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

    private fun getFilePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex: Int = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val filePath = it.getString(columnIndex)
                Log.e("RegisterActivity", "File path: $filePath")
                return filePath
            } else {
                Log.e("RegisterActivity", "Cursor is empty")
            }
        } ?: Log.e("RegisterActivity", "Cursor is null")
        return null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        spinnerTitle = findViewById(R.id.spinnerTitle)
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextDesignation = findViewById(R.id.editTextDesignation)
        editTextDivisionName = findViewById(R.id.editTextDivisionName)
        spinnerLabName = findViewById(R.id.spinnerLabName)
        editTextCityState = findViewById(R.id.editTextCityState)
        editTextIDCardNumber = findViewById(R.id.editTextIDCardNumber)
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto)
        imageViewPhotoPreview = findViewById(R.id.imageViewPhotoPreview)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextContactNumber = findViewById(R.id.editTextContactNumber)


        val titleOptions = resources.getStringArray(R.array.title_options)
        val titleAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, titleOptions)
        spinnerTitle.adapter = titleAdapter

        val labNames = fetchLabNamesFromDatabase()
        val labNameAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, labNames)
        spinnerLabName.adapter = labNameAdapter

        spinnerLabName.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val intent = Intent(this, LabNameActivity::class.java)
                labNameLauncher.launch(intent)
            }
            true
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
                    editTextPassword.text.toString(),
                    editTextEmail.text.toString(),
                    editTextContactNumber.text.toString()
                )

                Log.i("RegisterActivity", "User data: $user")

                // Upload user data to the server
                uploadUserData(user, photoPath)
            }
        }
    }

    private fun validateInput(): Boolean {
        val fullName = editTextFullName.text.toString().trim()
        val labName = spinnerLabName.selectedItem.toString().trim()
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

        val file = File(photoPath)
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
            .addFormDataPart("email", user.email)
            .addFormDataPart("contact", user.contact)
            .addFormDataPart(
                "photo",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.132:4000/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RegisterActivity", "Failed to upload user data", e)
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Failed to upload user data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            "User registered successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Failed to register user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun fetchLabNamesFromDatabase(): List<String> {
        // Replace this with your database query to fetch lab names
        val labNames = listOf("Lab 1", "Lab 2", "Lab 3") // Dummy data for demonstration
        return labNames
    }

    private fun getLabNameIndex(labName: String): Int {
        val labNames = spinnerLabName.adapter as ArrayAdapter<String>
        return labNames.getPosition(labName)
    }


    private fun fileExists(uri: Uri): Boolean {
        val file = File(uri.path)
        return file.exists()
    }
}
