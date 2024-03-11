package com.app.csir_npl

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.csir_npl.R.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject

class UpdateUserActivity : AppCompatActivity() {

    private var newUser =  User()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.update_user_layout)

        val address = intent.getStringExtra("address")
        val password = intent.getStringExtra("password")
        val email = intent.getStringExtra("email")

        val editTextUpdatedAddress: EditText = findViewById(id.editTextUpdatedAddress)
        val editPassword : EditText = findViewById(id.passwordEdit)
        val totpEdit : EditText = findViewById(id.totpEdit)

        editTextUpdatedAddress.setText(address)
        editPassword.setText(password)

        val buttonSave: Button = findViewById(id.buttonSave)
        val buttonCancel: Button = findViewById(id.buttonCancel)

        buttonSave.setOnClickListener {
            val updatedAddress = editTextUpdatedAddress.text.toString()
            val updatedPassword = editPassword.text.toString()
            val totpCode = totpEdit.text.toString()

            // Create an AlertDialog to prompt for current password
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter Current Password")

            // Set up the input field for password
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("OK") { dialog, which ->
                val currentPassword = input.text.toString()

                // Authenticate user with current password and TOTP code
                if (email != null) {
                    authenticateUser(email, currentPassword, updatedAddress, updatedPassword, totpCode)
                }
            }
            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            builder.show()
        }


        buttonCancel.setOnClickListener {
            finish()
        }
    }

    private fun authenticateUser(email: String, currentPassword: String, updatedAddress: String, updatedPassword: String, totpCode: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("currentPassword", currentPassword)
            .add("totpCode", totpCode) // Add TOTP code to the request
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.222:4000/updateRequestVerify")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@UpdateUserActivity, "Failed to authenticate user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        // Authentication successful, create a User object with updated data and update user on server
                        val updatedUser = User(email = email, address = updatedAddress, password = updatedPassword)
                        updateUserOnServer(updatedUser)
                    } else {
                        // Authentication failed, display error message
                        Toast.makeText(this@UpdateUserActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Function to update user details on the server
    private fun updateUserOnServer(updatedUser: User) {
        val url = "http://192.168.0.222:4000/updateDetails"

        val client = OkHttpClient()

        val jsonObject = JSONObject()
        jsonObject.put("email" , updatedUser.email)
        jsonObject.put("address", updatedUser.address)
        jsonObject.put("password" , updatedUser.password)

        val requestBody =
            jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseData = response.body?.string()
                        runOnUiThread {
                            Log.e("SUCCESS UPDATE" , "SUCCESS")
                            Toast.makeText(applicationContext, "Update successful", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {

                        val errorMessage = response.body?.string() ?: "Update failed"
                        runOnUiThread {
                            Log.e("FAILED UPDATE" , "FAILED")
                            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("API CALL FAILED" , "CALL FAILED")
                    runOnUiThread {
                        Log.e("API call failed", e.message ?: "Unknown error occurred")
                        Toast.makeText(applicationContext, "API call failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}
