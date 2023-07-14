package com.app.csir_npl
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextIdNumber: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextIdNumber = findViewById(R.id.editTextIdNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val idCardNumber = editTextIdNumber.text.toString()
            val password = editTextPassword.text.toString()
            authenticateUser(idCardNumber, password)
        }
    }

    private fun authenticateUser(idNumber: String, password: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("idCardNumber", idNumber)
            .add("password", password)
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

                Log.d("LoginActivity", "Response Body: $responseBody") // Log the response body

                runOnUiThread {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val message = jsonResponse.getString("message")
                            val user = jsonResponse.getJSONObject("user")
                            Log.d("LoginActivity", "Response Body: $responseBody") // Log the response body

                            if (user.length() > 0) {
                                val title = user.getString("title")
                                val name = user.getString("name")
                                val photoPath = user.getString("photoUrl")
                                val designation = user.getString("designation")
                                val division = user.getString("division")
                                val lab = user.getString("lab")
                                val idCardNumber = user.getString("id")
                                val emaiId = user.getString("email")
                                val contact = user.getString("contact")
                                val status = user.getString("status")
                                val autho = user.getString("autho")

                                if (status == "unverified" || status == "User Revoked") {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "User unverified or revoked by admin",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {

                                    // Authentication successful, proceed to the next activity
                                    val intent =
                                        Intent(this@LoginActivity, IdCardActivity::class.java)
                                    intent.putExtra("title", title)
                                    intent.putExtra("name", name)
                                    intent.putExtra("photoPath", photoPath)
                                    intent.putExtra("designation", designation)
                                    intent.putExtra("division", division)
                                    intent.putExtra("lab", lab)
                                    intent.putExtra("idCardNumber", idCardNumber)
                                    intent.putExtra("emailId", emaiId)
                                    intent.putExtra("contact", contact)
                                    intent.putExtra("status", status)
                                    intent.putExtra("autho", autho)
                                    startActivity(intent)
                                    finish()
                                }
                            }else {
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
