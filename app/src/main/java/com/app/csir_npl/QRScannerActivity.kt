package com.app.csir_npl

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.File
import java.io.FileOutputStream

class QRScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scannerView: ZXingScannerView? = null

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is granted, initialize scanner
            initializeScanner()
        }
    }

    private fun initializeScanner() {
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, initialize scanner
                    initializeScanner()
                } else {
                    // Permission denied, show a message or handle it gracefully
                    Toast.makeText(
                        this,
                        "Camera permission denied. Cannot scan QR codes.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish() // Close the activity if permission is denied
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView?.stopCamera()
    }

    override fun handleResult(result: Result?) {
        val vcfData = result?.text ?: ""
        Log.d("QRScanner", "Scanned VCF data: $vcfData")

        // Show dialog to save contact
        showSaveContactDialog(vcfData)

        // Resume camera preview
        scannerView?.resumeCameraPreview(this)
    }

    private fun showSaveContactDialog(vcfData: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save Contact")
        builder.setMessage("Do you want to save this contact?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            saveVcfDataAsContact(vcfData)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun saveVcfDataAsContact(vcfData: String) {
        try {
            val intent = Intent(Intent.ACTION_INSERT)
            intent.type = ContactsContract.Contacts.CONTENT_TYPE

            // Split the VCF data into individual lines
            val lines = vcfData.trim().split("\n")

            // Extract relevant fields from the VCF data
            var name = ""
            var organization = ""
            var email = ""
            var phone = ""

            for (line in lines) {
                val parts = line.split(":")
                if (parts.size >= 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    when (key) {
                        "FN" -> name = value
                        "ORG" -> organization = value
                        "TEL" -> phone = value
                        "EMAIL" -> email = value
                    }
                }
            }

            // Set the extracted fields in the intent
            intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
            intent.putExtra(ContactsContract.Intents.Insert.COMPANY, organization)
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email)

            // Start the activity to save the contact
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save contact", Toast.LENGTH_SHORT).show()
        }
    }



}
