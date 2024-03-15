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
    private lateinit var editTextTotp: EditText
    private lateinit var buttonLogin: Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextTotp = findViewById(R.id.editTextTotp)
        buttonLogin = findViewById(R.id.buttonLogin)
        progressBar = findViewById(R.id.progressBar)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val totp = editTextTotp.text.toString()
            progressBar.visibility = View.VISIBLE
            requestOtp(email, password, totp)
        }
    }

    private fun requestOtp(email: String, password: String, totp: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("email", email)
            .add("password", password)
            .add("totp", totp) // Include TOTP code in the request
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.222:4000/login") // Assuming login URL accepts email, password, and TOTP
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Failed to authenticate", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                Log.d("LoginActivity", "Response Body: $responseBody")

                runOnUiThread {
                    progressBar.visibility = View.GONE
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
                                    val fullName = "$title $name" // need change
                                    intent.putExtra("full_name", fullName)
                                    intent.putExtra("photoPath", photoPath)
                                    intent.putExtra("designation", designation)
                                    intent.putExtra("division", division)
                                    intent.putExtra("subDivision", subDivision)
                                    intent.putExtra("lab", lab)
                                    intent.putExtra("idCardNumber", idCardNumber)
                                    intent.putExtra("emailId", email)
                                    intent.putExtra("contact", contact)
                                    intent.putExtra("status", status)
                                    intent.putExtra("password", password)
                                    intent.putExtra("autho", autho)
                                    intent.putExtra("logoUrl", logoUrl)
                                    intent.putExtra("address", address)
                                    intent.putExtra("emergency", emergency)
                                    intent.putExtra("bGroup", bGroup)
                                    Log.e("Intent:", "$intent")
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Empty user object. Authentication failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@LoginActivity, "Failed to parse response", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Authentication failed, Please check credentials",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}
