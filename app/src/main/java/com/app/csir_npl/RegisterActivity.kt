package com.app.csir_npl

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import android.content.Context

import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSendOTP: Button
    private lateinit var editTextOTP: EditText
    private lateinit var buttonSubmitOTP: Button
    private lateinit var editTextIDCardNumber: EditText
    private lateinit var editTextContact: EditText
    private lateinit var imageViewPhotoPreview: ImageView
    private lateinit var buttonUploadPhoto: Button
    private val labNameList: MutableList<String> = mutableListOf()
    private var generatedOTP: String = "" // Variable to store the generated OTP
    private val PICK_IMAGE_REQUEST_CODE = 100
    private val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 101
    private var selectedImageUri: Uri? = null
    private var photoPath: String = ""
    private lateinit var spinnerLabName: Spinner
    private lateinit var progressBar: ProgressBar




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        editTextEmail = findViewById(R.id.editTextEmail)
        imageViewPhotoPreview = findViewById(R.id.imageViewPhotoPreview)
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto)
        spinnerLabName = findViewById(R.id.spinnerLabName)
        progressBar = findViewById(R.id.progressBar)


        val labNameAdapter = LabNameAdapter(this, labNameList)
//        labNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLabName.adapter = labNameAdapter

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.0.222:4000/labnames")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to fetch LabName options",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val responseBody = response.body?.string()
                val labNamesJsonArray = JSONArray(responseBody)
                labNameList.clear()
                for (i in 0 until labNamesJsonArray.length()) {
                    val labName = labNamesJsonArray.getString(i)
                    labNameList.add(labName)
                }
                runOnUiThread {
                    labNameAdapter.notifyDataSetChanged()
                }
            }
        })

        findViewById<Button>(R.id.buttonProceed).setOnClickListener {
            navigateToSecretKeyLayout()
        }
        buttonUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }
    }

    private fun navigateToSecretKeyLayout() {
        // Inflate layout for displaying secret key
        val secretKeyLayout = layoutInflater.inflate(R.layout.layout_secret_key, null)
        setContentView(secretKeyLayout)

        // Generate and display secret key
        generateSecretKeyFromServer(object : SecretKeyCallback {
            override fun onSecretKeyReceived(secretKey: String?) {
                if (secretKey != null) {
                    findViewById<TextView>(R.id.textViewSecretKey).text = secretKey

                    // Handle click event for "Copy" button
                    findViewById<Button>(R.id.buttonCopy).setOnClickListener {
                        copySecretKeyToClipboard(secretKey)
                    }

                    // Handle click event for "Open" button
                    findViewById<Button>(R.id.buttonOpen).setOnClickListener {
                        openGoogleAuthenticatorSetup(secretKey)
                        checkMasterTableAndSaveUser(secretKey)
                    }
                } else {
                    // Handle the case when the secret key is null
                    Toast.makeText(applicationContext, "Failed to fetch secret key", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun generateSecretKeyFromServer(callback: SecretKeyCallback) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.0.222:4000/generateSecret")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure, such as showing an error message
                runOnUiThread {
                    callback.onSecretKeyReceived(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val secretKey = jsonResponse.optString("secretKey", "")
                    runOnUiThread {
                        callback.onSecretKeyReceived(secretKey)
                    }
                } else {
                    runOnUiThread {
                        callback.onSecretKeyReceived(null)
                    }
                }
            }

        })
    }

    interface SecretKeyCallback {
        fun onSecretKeyReceived(secretKey: String?)
    }



    private fun copySecretKeyToClipboard(secretKey: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Secret Key", secretKey)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this, "Secret key copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun openGoogleAuthenticatorSetup(secretKey: String) {
        val playStoreUrl = "https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(playStoreUrl)

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Play Store app not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(filePathColumn[0])
            val filePath = it.getString(columnIndex)
            it.close()
            return filePath
        }
        return null
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                handleSelectedImage(selectedImageUri)
            }
        }
    }



    private fun generateOTP(): String {
        val random = (1000..9999).random()
        return random.toString()
    }

    private fun sendOTPRequest(email: String) {
        val client = OkHttpClient()

        // Create a JSON object with the email data
        val requestBody = JSONObject().apply {
            put("email", email)
        }

        // Create a request to send the email data to the server
        val request = Request.Builder()
            .url("http://192.168.0.222:4000/send-otp")
            .post(requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        // Send the request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(applicationContext, "You are not registered in database. Pls, Contact LAB ADMIN.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful) {
                        progressBar.visibility = View.GONE
                        Log.e("Response body with OTP", "${responseBody.toString()}")
                        val otpJson = JSONObject(responseBody)
                        val otp = otpJson.getString("otp")
                        generatedOTP = otp

                        Toast.makeText(
                            applicationContext,
                            "OTP sent to registered EMAIL ID",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            applicationContext,
                            "You are not registered in database. Please, Contact LAB ADMIN.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun checkMasterTableAndSaveUser(secretKey: String) {
        val client = OkHttpClient()

        // Get the values from the EditText and ImageView
        val email = editTextEmail.text.toString().trim()
        val photoBitmap = (imageViewPhotoPreview.drawable as BitmapDrawable).bitmap
        val labCode = extractLabCode(spinnerLabName.selectedItem.toString())

        // Create a temporary file to store the photo
        val photoFile = File.createTempFile("photo", ".jpg", cacheDir)
        val outputStream = FileOutputStream(photoFile)
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.close()

        // Create a multipart/form-data request body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("email", email)
            .addFormDataPart("labCode", labCode )
            .addFormDataPart(
                "photo",
                photoFile.name,
                photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .addFormDataPart("secretKey", secretKey)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.222:4000/register")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Failed to register user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed to register user",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun extractLabCode(labName: String): String {
        val startIndex = labName.indexOf('(')
        val endIndex = labName.indexOf(')')
        return if (startIndex != -1 && endIndex != -1) {
            labName.substring(startIndex + 1, endIndex)
        } else {
            labName
        }
    }
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            getContent.launch(intent)
        } else {
            Toast.makeText(
                this,
                "No app found to handle the image picker.",
                Toast.LENGTH_SHORT
            ).show()
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

                photoPath = getFilePathFromUri(selectedImageUri!!) ?: ""
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    "File not found",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    private fun handleSelectedImage(uri: Uri) {
        try {
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imageViewPhotoPreview.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot open image picker.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

