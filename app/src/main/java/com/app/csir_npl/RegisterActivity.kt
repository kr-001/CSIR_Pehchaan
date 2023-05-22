package com.app.csir_npl
import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.app.csir_npl.User
import com.app.csir_npl.databinding.ActivityRegisterBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextFullName: EditText
    private lateinit var editTextDesignation: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextMobile: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var editTextId: EditText // Add this line
    private lateinit var buttonRegister: Button
    private lateinit var buttonUploadImage: Button
    private lateinit var spinnerDuName: Spinner // Add this line
    private lateinit var spinnerDpName: Spinner // Add this line

    private lateinit var firestore: FirebaseFirestore
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

        firestore = FirebaseFirestore.getInstance()

        editTextFullName = binding.editTextFullName
        editTextDesignation = binding.editTextDesignation
        editTextEmail = binding.editTextEmail
        editTextMobile = binding.editTextMobile
        editTextAddress = binding.editTextAddress
        editTextPassword = binding.editTextPassword
        editTextConfirmPassword = binding.editTextConfirmPassword
        editTextId = binding.editTextIDNumber // Add this line
        buttonRegister = binding.buttonSubmit
        buttonUploadImage = binding.buttonUploadProfilePicture
        spinnerDuName = binding.spinnerDuName // Add this line
        spinnerDpName = binding.spinnerDpName // Add this line

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
            val id = editTextId.text.toString() // Retrieve ID value
            val duName = spinnerDuName.selectedItem?.toString() ?: "" // Get selected DU name
            val dpName = spinnerDpName.selectedItem?.toString() ?: "" // Get selected DP name

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

            // Create a User object with the field values
            val user = User(
                id,
                fullName,
                designation,
                email,
                mobile,
                address,
                password,
                confirmPassword,
                duName,
                dpName,
                ""
            )

            // Save the user data to Firestore
            saveUserData(user, imageFile)
        }

        loadDuNames()
        loadDpNames()
    }

    private fun createImageFile(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val tempDir = File(cacheDir, "temp")
        if (!tempDir.exists()) {
            tempDir.mkdirs()
        }

        val tempFile = File(tempDir, "temp_image")
        tempFile.createNewFile()

        val outputStream = FileOutputStream(tempFile)
        val buffer = ByteArray(4 * 1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()

        return tempFile
    }

    private fun saveUserData(user: User, imageFile: File) {
        // Upload the image file to Firebase Storage and get the download URL
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_images/${user.id}.jpg")
        val uploadTask = imageRef.putFile(Uri.fromFile(imageFile))

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                user.profilePictureUrl = downloadUri.toString()

                // Save the user object to Firestore
                firestore.collection("users")
                    .document(user.id)
                    .set(user) // Use set() instead of add() to update an existing document
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error saving user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Error uploading image: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDuNames() {
        firestore.collection("du_names")
            .get()
            .addOnSuccessListener { documents ->
                val duNames = ArrayList<String>()
                for (document in documents) {
                    val duName = document.getString("name")
                    duName?.let { duNames.add(it) }
                }
                setupDuNameSpinner(duNames)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading DU names: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDpNames() {
        firestore.collection("dp_names")
            .get()
            .addOnSuccessListener { documents ->
                val dpNames = ArrayList<String>()
                for (document in documents) {
                    val dpName = document.getString("name")
                    dpName?.let { dpNames.add(it) }
                }
                setupDpNameSpinner(dpNames)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading DP names: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDuNameSpinner(duNames: List<String>) {
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, duNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDuName.adapter = adapter
    }

    private fun setupDpNameSpinner(dpNames: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dpNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDpName.adapter = adapter
    }
}
