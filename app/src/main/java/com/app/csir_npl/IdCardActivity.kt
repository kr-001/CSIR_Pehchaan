package com.app.csir_npl

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File
import java.io.FileOutputStream

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

        val user = intent.getSerializableExtra("user") as? User

        if (user != null) {
            // Set user details
            val photoBitmap = BitmapFactory.decodeFile(user.photoPath)
            imageViewPhoto.setImageBitmap(photoBitmap)
            textViewTitle.text = user.title
            textViewFullName.text = user.fullName
            textViewDesignation.text = user.designation
            textViewDivisionName.text = user.divisionName
            textViewLabName.text = user.labName
            textViewCityState.text = user.cityState
            textViewIdCardNumber.text = user.idCardNumber

            buttonShareVcf.setOnClickListener {
                shareVcfFile(user)
            }

            buttonGenerateQR.setOnClickListener {
                generateQRCode(user)
            }
        }
    }

    private fun shareVcfFile(user: User) {
        // Create a VCF file and save it locally
        val vcfFile = File(cacheDir, "contact.vcf")
        val vcfData = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "FN:${user.fullName}\n" +
                "TITLE:${user.title}\n" +
                "ORG:${user.divisionName}\n" +
                "END:VCARD"

        FileOutputStream(vcfFile).use { stream ->
            stream.write(vcfData.toByteArray())
        }

        // Create a share intent and set the VCF file
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/x-vcard"
        shareIntent.putExtra(Intent.EXTRA_STREAM, vcfFile)

        // Start the activity to share the VCF file
        startActivity(Intent.createChooser(shareIntent, "Share Contact"))
    }

    private fun generateQRCode(user: User) {
        try {
            // Generate QR code from user details
            val qrData = "BEGIN:VCARD\n" +
                    "VERSION:3.0\n" +
                    "FN:${user.fullName}\n" +
                    "TITLE:${user.title}\n" +
                    "ORG:${user.divisionName}\n" +
                    "END:VCARD"

            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 500, 500)

            // Display the QR code
            imageViewPhoto.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
