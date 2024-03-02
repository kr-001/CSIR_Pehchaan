
package com.app.csir_npl
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.csir_npl.R.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class UpdateUserActivity : AppCompatActivity() {

    private var newUser =  User()
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.update_user_layout)
        val title = intent.getStringExtra("title")
        val card = intent.getStringExtra("CardNumber")
        val designation = intent.getStringExtra("designation")
        val division = intent.getStringExtra("division")
        val lab = intent.getStringExtra("lab")
        val address = intent.getStringExtra("address")
        val password = intent.getStringExtra("password")
        val email = intent.getStringExtra("email")

        val editTextUpdatedTitle: EditText = findViewById(id.editTextUpdatedTitle)
        val editTextCardNumber : EditText = findViewById(id.editTextUpdatedCardNumber)
        val editTextUpdatedDesignation: EditText = findViewById(id.editTextUpdatedDesignation)
        val editTextUpdatedDivision: EditText = findViewById(id.editTextUpdatedDivision)
        val editTextUpdatedLabName: EditText = findViewById(id.editTextUpdatedLabName)
        val editTextUpdatedAddress: EditText = findViewById(id.editTextUpdatedAddress)
        val editPassword : EditText = findViewById(id.passwordEdit)


        editTextUpdatedTitle.setText(title)
        editTextUpdatedDesignation.setText(designation)
        editTextUpdatedDivision.setText(division)
        editTextUpdatedLabName.setText(lab)
        editTextUpdatedAddress.setText(address)
        editTextCardNumber.setText(card)
        editPassword.setText(password)

        val buttonSave: Button = findViewById(id.buttonSave)
        buttonSave.setOnClickListener {
            val updatedTitle = editTextUpdatedTitle.text.toString()
            val cardNumber = editTextCardNumber.text.toString()
            val updatedDesignation = editTextUpdatedDesignation.text.toString()
            val updatedDivision = editTextUpdatedDivision.text.toString()
            val updatedLabName = editTextUpdatedLabName.text.toString()
            val updatedAddress = editTextUpdatedAddress.text.toString()
            val updatedPassword = editPassword.text.toString()

            val updatedUserOnServer = User(
                title = updatedTitle,
                idCardNumber = cardNumber,
                designation = updatedDesignation,
                divisionName = updatedDivision,
                labName = updatedLabName,
                address = updatedAddress,
                password = updatedPassword
            )
            if (email != null) {
                requestOtp(email,password)
            }
            newUser = updatedUserOnServer
        }
    }
    private fun requestOtp(email: String, password: String?) {
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password.toString())
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.222:4000/send-otp") // Replace with your backend URL
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@UpdateUserActivity, "Failed to request OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.e("Response Body: ", "$responseBody")
                runOnUiThread {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val success = jsonResponse.getString("message")
                            if (success.isNotEmpty()) {
                                showOtpPopup()
                            } else {
                                Toast.makeText(this@UpdateUserActivity, "FAILED TO PARSE RESPONSE", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@UpdateUserActivity, "Failed to Parse Response", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this@UpdateUserActivity, "Failed to request OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    private fun authenticateUser(email: String, otp: String) {
        val client = OkHttpClient()

        // Create a JSON request body with email and OTP
        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("otp", otp)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.222:4000/verify-otp")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@UpdateUserActivity, "Failed to verify OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val success = jsonResponse.getBoolean("success")
                            if (success) {
                                updateUserOnServer(newUser)
                            } else {
                                Toast.makeText(this@UpdateUserActivity, "OTP verification failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@UpdateUserActivity, "Failed to Parse Response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@UpdateUserActivity, "Failed to verify OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun showOtpPopup() {
        val dialog = android.app.Dialog(this@UpdateUserActivity)
        dialog.setContentView(layout.dialog_otp)
        val email = intent.getStringExtra("email")
        val editTextOtp: EditText = dialog.findViewById(R.id.editTextOtp)
        val buttonVerify: Button = dialog.findViewById(R.id.buttonVerify)

        buttonVerify.setOnClickListener {
            val otp = editTextOtp.text.toString()
            if (otp.isNotEmpty()) {
                dialog.dismiss()
                // Verify OTP with your backend
                if (email != null) {
                    authenticateUser(email, otp)
                }
            } else {
                Toast.makeText(this@UpdateUserActivity, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    private fun updateUserOnServer(updatedUser: User) {
        val url = "http://192.168.0.222:4000/updateDetails"

        val client = OkHttpClient()

        val jsonObject = JSONObject()
        jsonObject.put("title", updatedUser.title)
        jsonObject.put("CardNumber", updatedUser.idCardNumber)
        jsonObject.put("designation", updatedUser.designation)
        jsonObject.put("division", updatedUser.divisionName)
        jsonObject.put("lab", updatedUser.labName)
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



