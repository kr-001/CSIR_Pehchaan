package com.app.csir_npl
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
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
            .url("http://192.168.0.222:3000/login") // Replace <YOUR_SERVER_HOST> with your server's IP address or domain name
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
                        val jsonResponse = JSONArray(responseBody)
                        if (jsonResponse.length() > 0) {
                            val user = jsonResponse.getJSONObject(0)
                            val name = user.getString("fullName")
                            val photoPath = user.getString("photoPath")

                            // Authentication successful, proceed to the next activity
                            val intent = Intent(this@LoginActivity, IdCardActivity::class.java)
                            intent.putExtra("name", name)
                            intent.putExtra("photoPath", photoPath)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }



        })
    }
}
