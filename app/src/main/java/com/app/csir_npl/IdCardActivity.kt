package com.app.csir_npl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File

class IdCardActivity : AppCompatActivity() {
    private lateinit var imageViewPhoto: ImageView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewFullName: TextView
    private lateinit var textViewDesignation: TextView
    private lateinit var textViewDivisionName: TextView
    private lateinit var textViewLabName: TextView
    private lateinit var textViewCityState: TextView
    private lateinit var textViewIdCardNumber: TextView
    private lateinit var buttonShareVcf: Button
    private lateinit var buttonGenerateQR: Button
    private lateinit var imageViewQRCode: ImageView
    private lateinit var photoPath: String

    companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_card)

        imageViewPhoto = findViewById(R.id.imageViewPhoto)
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewFullName = findViewById(R.id.textViewFullName)
        textViewDesignation = findViewById(R.id.textViewDesignation)
        textViewDivisionName = findViewById(R.id.textViewDivisionName)
        textViewLabName = findViewById(R.id.textViewLabName)
        textViewCityState = findViewById(R.id.textViewCityState)
        textViewIdCardNumber = findViewById(R.id.textViewIdCardNumber)
        buttonShareVcf = findViewById(R.id.buttonShareVcf)
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR)
        imageViewQRCode = findViewById(R.id.imageViewQRCode)

        val title = intent.getStringExtra("title")
        val name = intent.getStringExtra("name")
        photoPath = intent.getStringExtra("photoPath").toString() // Removed the 'val' keyword here
        val designation = intent.getStringExtra("designation")
        val division = intent.getStringExtra("division")
        val cityState = intent.getStringExtra("cityState")
        val lab = intent.getStringExtra("lab")
        val idCardNumber = intent.getStringExtra("idCardNumber")
        val emailId = intent.getStringExtra("emailID")
        val contact = intent.getStringExtra("contact")
        val status = intent.getStringExtra("status")
        val autho = intent.getStringExtra("autho")

        if (name != null && photoPath != null) {
            // Set user details
            Log.e("PhotoPath: ", "$photoPath")
            loadPhoto(photoPath)
            textViewTitle.text = title
            textViewFullName.text = name
            textViewDesignation.text = designation
            textViewDivisionName.text = division
            textViewLabName.text = lab
            textViewCityState.text = cityState
            textViewIdCardNumber.text = idCardNumber

            buttonShareVcf.setOnClickListener {
                shareVcfFile(name)
            }

            buttonGenerateQR.setOnClickListener {
                if (idCardNumber != null) {
                    if (lab != null) {
                        if (emailId != null) {
                            if (contact != null) {
                                if (status != null) {
                                    if (autho != null) {
                                        generateQRCode(name, lab, idCardNumber, emailId, contact, status, autho)
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
            Log.e("IdCardActivity", "photoPath is null")
        }
    }

    private fun loadPhoto(photoPath: String) {
        // Check if the app has permission to read external storage
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            // Permission is already granted, load the photo
            val photoFile = File(photoPath)
            if (photoFile.exists()) {
                val photoBitmap = BitmapFactory.decodeFile(photoPath)
                Log.e("BITMAP", "$photoBitmap")
                imageViewPhoto.setImageBitmap(photoBitmap)
            } else {
                Log.e("IdCardActivity", "Photo file does not exist")
            }
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, load the photo
                    loadPhoto(photoPath)
                } else {
          Log.e("PHOTOPATH" , "ERROR $photoPath")
                }
            }
        }
    }

    private fun shareVcfFile(name: String) {
        // Create a VCF file and save it locally
        val vcfFile = File(cacheDir, "contact.vcf")
        val vcfData = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "FN:$name\n" +
                "END:VCARD"

        vcfFile.writeText(vcfData)

        // Create a share intent and set the VCF file
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/x-vcard"
        shareIntent.putExtra(Intent.EXTRA_STREAM, vcfFile)

        // Start the activity to share the VCF file
        startActivity(Intent.createChooser(shareIntent, "Share Contact"))
    }

    private fun generateQRCode(name: String, lab: String, idCardNumber: String, emailID: String, contact: String, status: String, autho: String) {
        try {
            // Generate QR code from user details
            val qrData = "BEGIN:VCARD\n" +
                    "VERSION:3.0\n" +
                    "FN:$name\n" +
                    "LAB:$lab\n" +
                    "ID:$idCardNumber\n" +
                    "EMAIL:$emailID\n" +
                    "CONTACT:$contact\n" +
                    "STATUS:$status\n" +
                    "AUTHO:$autho\n" +
                    "END:VCARD"

            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap =
                barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 500, 500)

            // Display the QR code
            imageViewQRCode.setImageBitmap(bitmap)
            imageViewQRCode.visibility = ImageView.VISIBLE
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }


}
