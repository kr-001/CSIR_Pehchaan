import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.csir_npl.MainActivity
import com.app.csir_npl.databinding.ActivityRegisterBinding
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextFullName: EditText
    private lateinit var editTextDesignation: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextMobile: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var buttonUploadImage: Button

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var selectedImageUri: Uri

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editTextFullName = binding.editTextFullName
        editTextDesignation = binding.editTextDesignation
        editTextEmail = binding.editTextEmail
        editTextMobile = binding.editTextMobile
        editTextAddress = binding.editTextAddress
        editTextPassword = binding.editTextPassword
        editTextConfirmPassword = binding.editTextConfirmPassword
        buttonRegister = binding.buttonRegister
        buttonUploadImage = binding.buttonUploadProfilePicture

        databaseHelper = DatabaseHelper()

        buttonUploadImage.setOnClickListener {
            getContent.launch("image/*")
        }

        buttonRegister.setOnClickListener {
            val fullName = editTextFullName.text.toString()
            val designation = editTextDesignation.text.toString()
            val email = editTextEmail.text.toString()
            val mobile = editTextMobile.text.toString()
            val address = editTextAddress.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!::selectedImageUri.isInitialized) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imageFile = createImageFile(selectedImageUri)
            if (imageFile == null) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Insert user details into the database
            databaseHelper.insertUserDetails(
                fullName,
                designation,
                email,
                mobile,
                address,
                password,
                imageFile
            )

            // Redirect to MainPage activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createImageFile(uri: Uri): File? {
        val contentResolver = contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val fileName = System.currentTimeMillis().toString() + "_" + uri.lastPathSegment
        val imageFile = File(cacheDir, fileName)
        val outputStream = FileOutputStream(imageFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return imageFile
    }
}
