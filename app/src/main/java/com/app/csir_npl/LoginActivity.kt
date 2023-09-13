package com.app.csir_npl

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            progressBar.visibility = View.VISIBLE
            requestOtp(email, password)
        }
    }

    private fun requestOtp(idNumber: String, password: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("email", idNumber)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.132:4000/request-otp") //
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Failed to request OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                Log.d("LoginActivity", "Response Body: $responseBody")

                runOnUiThread {
                    if (response.isSuccessful) {
                        progressBar.visibility = View.GONE
                        try {
                            val jsonResponse = JSONObject(responseBody)

                            val success = jsonResponse.getBoolean("success")

                            if (success) {
                                showOtpPopup()
                            } else {
                                Toast.makeText(this@LoginActivity, "Failed to request OTP", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@LoginActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Failed to request OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun showOtpPopup() {
        val dialog = Dialog(this@LoginActivity)
        dialog.setContentView(R.layout.dialog_otp) // Replace with the layout file for your OTP dialog

        val editTextOtp: EditText = dialog.findViewById(R.id.editTextOtp)
        val buttonVerify: Button = dialog.findViewById(R.id.buttonVerify)

        buttonVerify.setOnClickListener {
            val otp = editTextOtp.text.toString()
            if (otp.isNotEmpty()) {
                dialog.dismiss()
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()
                authenticateUser(email, password, otp)
            } else {
                Toast.makeText(this@LoginActivity, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    private fun authenticateUser(idNumber: String, password: String, otp: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("email", idNumber)
            .add("password", password)
            .add("otp", otp)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.132:4000/login") // Replace <YOUR_SERVER_HOST> with your server's IP address or domain name
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Failed to authenticate", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                Log.d("LoginActivity", "Response Body: $responseBody")

                runOnUiThread {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val message = jsonResponse.getString("message")
                            val user = jsonResponse.getJSONObject("user")
                            Log.d("LoginActivity", "Response Body: $responseBody")

                            if (user.length() > 0) {
                                val title = user.getString("title")
                                val name = user.getString("name")
                                val photoPath = user.getString("photoUrl")
                                val designation = user.getString("designation")
                                val division = user.getString("division")
                                val subDivision = user.getString("subDivision")
                                val lab = user.getString("lab")
                                val idCardNumber = user.getString("id")
                                val emailId = user.getString("email")
                                val contact = user.getString("contact")
                                val status = user.getString("status")
                                val autho = user.getString("autho")
                                val logoUrl = user.getString("logoUrl")
                                val address = user.getString("address")
                                val password = user.getString("password")
                                val emergency = user.getString("emergency")
                                val bGroup = user.getString("bGroup")

                                if (status == "unverified" || status == "User Revoked") {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "User unverified or revoked by admin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    val intent = Intent(this@LoginActivity, IdCardActivity::class.java)
                                    intent.putExtra("title", title)
                                    intent.putExtra("name", name)
                                    intent.putExtra("photoPath", photoPath)
                                    intent.putExtra("designation", designation)
                                    intent.putExtra("division", division)
                                    intent.putExtra("subDivision",subDivision)
                                    intent.putExtra("lab", lab)
                                    intent.putExtra("idCardNumber", idCardNumber)
                                    intent.putExtra("emailId", emailId)
                                    intent.putExtra("contact", contact)
                                    intent.putExtra("status", status)
                                    intent.putExtra("password",password)
                                    intent.putExtra("autho", autho)
                                    intent.putExtra("logoUrl", logoUrl)
                                    intent.putExtra("address",address)
                                    intent.putExtra("emergency",emergency)
                                    intent.putExtra("bGroup",bGroup)
                                    Log.e("Intent:","$intent")
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                Toast.makeText(this@LoginActivity, "Empty user object. Authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@LoginActivity, "Failed to parse response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
