package  com.app.csir_npl

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PendingIntent
import android.provider.Settings
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.nio.charset.Charset

class IdCardActivity : AppCompatActivity() {
    private lateinit var imageViewPhoto: ImageView
    private lateinit var textViewFullName: TextView
    private lateinit var textViewDesignation: TextView
    private lateinit var textViewDivisionName: TextView
    private lateinit var textViewSubDivisionName: TextView
    private lateinit var textViewLabName: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewIdCardNumber: TextView
    private lateinit var textViewBloodGroup: TextView
    private lateinit var textViewAutho: TextView
    private lateinit var buttonGenerateQR: Button
    private lateinit var imageViewQRCode: ImageView
    private lateinit var photoPath: String
    private lateinit var imageViewLogoRight: ImageView
    private lateinit var emergencyContact: TextView
    private lateinit var textViewStatus: TextView
    private lateinit var qrCodeDialog: Dialog
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var nfcShareDialog: Dialog
    private val NFC_PERMISSION_REQUEST_CODE = 100

    companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_card)


        imageViewPhoto = findViewById(R.id.imageViewPhoto)
        textViewFullName = findViewById(R.id.textViewFullName)
        textViewDesignation = findViewById(R.id.textViewDesignation)
        textViewDivisionName = findViewById(R.id.textViewDivisionName)
        textViewSubDivisionName = findViewById(R.id.textViewSubDivisionName)
        textViewLabName = findViewById(R.id.textViewLabName)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewIdCardNumber = findViewById(R.id.textViewIdCardNumber)
        textViewAutho = findViewById(R.id.textViewAuthoName)
        emergencyContact = findViewById(R.id.textViewEmergencyName)
        textViewBloodGroup = findViewById(R.id.textViewBloodGroup)
        imageViewLogoRight = findViewById(R.id.imageViewLogoRight)
        textViewStatus = findViewById(R.id.textViewVeriStatus)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val name = intent.getStringExtra("full_name")
        if (name != null) {
            Log.d("FULL NAME: ", name)
        }
        photoPath = intent.getStringExtra("photoPath").toString()
        val typeface = Typeface.createFromAsset(assets, "tnr.ttf")
        textViewFullName.typeface = typeface
        textViewDesignation.typeface = typeface
        textViewAddress.typeface = typeface
        textViewAutho.typeface = typeface
        textViewDesignation.typeface = typeface
        textViewSubDivisionName.typeface = typeface
        textViewIdCardNumber.typeface = typeface
        textViewBloodGroup.typeface = typeface
        textViewStatus.typeface = typeface
        textViewLabName.typeface = typeface
        textViewDivisionName.typeface = typeface
        emergencyContact.typeface = typeface
        val photoUrl = photoPath
        val correctedPhotoUrl = photoUrl.replace("\\", "/")
        val designation = intent.getStringExtra("designation")
        val division = intent.getStringExtra("division")
        val subDivision = intent.getStringExtra("subDivision")
        val address = intent.getStringExtra("address")
        val emergency = intent.getStringExtra("emergency")
        val lab = intent.getStringExtra("lab")
        email = intent.getStringExtra("emailId").toString()
        password = intent.getStringExtra("password").toString()
        val autho = intent.getStringExtra("autho")
        val status = intent.getStringExtra("status")
        val idCardNumber = intent.getStringExtra("idCardNumber")
        val bGroup = intent.getStringExtra("bGroup")
        val logoUrl = intent.getStringExtra("logoUrl")
        if (logoUrl != null) {
            loadLogo(logoUrl)
        };



        if (name != null && photoPath != null) {
            Log.i("PhotoPath: ", "$correctedPhotoUrl")
            loadPhoto(correctedPhotoUrl)
            textViewFullName.text = name
            textViewDesignation.text = designation
            textViewDivisionName.text = division
            textViewLabName.text = lab
            textViewAddress.text = address
            textViewIdCardNumber.text = idCardNumber
            textViewBloodGroup.text = bGroup
            textViewSubDivisionName.text = subDivision
            textViewAutho.text = autho
            textViewStatus.text = status

            if (emergency == "null") {
                emergencyContact.text = "Not Available"
            } else {
                emergencyContact.text = emergency
                emergencyContact.visibility = View.VISIBLE
            }

        } else {
            Log.e("IdCardActivity", "photoPath is null")
        }


    }
    private fun showNfcShareDialog() {
        val imageViewStatus = nfcShareDialog.findViewById<ImageView>(R.id.imageViewStatus)
        val textViewStatus = nfcShareDialog.findViewById<TextView>(R.id.textViewStatus)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        nfcShareDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        nfcShareDialog.show()
        imageViewStatus.startAnimation(slideUp)
        textViewStatus.startAnimation(slideUp)
    }

    private fun hideNfcShareDialog() {
        val imageViewStatus = nfcShareDialog.findViewById<ImageView>(R.id.imageViewStatus)
        val textViewStatus = nfcShareDialog.findViewById<TextView>(R.id.textViewStatus)
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        imageViewStatus.startAnimation(slideDown)
        textViewStatus.startAnimation(slideDown)
        slideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                nfcShareDialog.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
    private fun loadPhoto(correctedPhotoUrl: String) {
        Glide.with(this)
            .load(correctedPhotoUrl)
            .into(imageViewPhoto)
    }

    private fun loadLogo(logoUrl: String) {
        Glide.with(this)
            .load(logoUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(imageViewLogoRight)
    }


    private fun showDetailsSelectionPopup() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Select Details to Include in QR Code")

        val view = layoutInflater.inflate(R.layout.dialog_qr_code_selection, null)
        dialogBuilder.setView(view)

        //04-03-2024- Kr.Ravi
        val checkboxDesignation = view.findViewById<CheckBox>(R.id.checkboxDesignation);
        val checkboxLabName = view.findViewById<CheckBox>(R.id.checkboxLabName);

        val checkboxEmail = view.findViewById<CheckBox>(R.id.checkboxEmail)
        val checkboxContact = view.findViewById<CheckBox>(R.id.checkboxContact)

        dialogBuilder.setPositiveButton("Generate") { dialog, which ->
            val name = textViewFullName.text.toString()
            val lab = textViewLabName.text.toString()
            val idCardNumber = textViewIdCardNumber.text.toString()

            val emailID =
                if (checkboxEmail.isChecked) intent.getStringExtra("emailId").toString() else ""
            val contact =
                if (checkboxContact.isChecked) intent.getStringExtra("contact").toString() else ""
            val designation =
                if (checkboxDesignation.isChecked) textViewDesignation.text.toString() else ""
            val division = if (checkboxLabName.isChecked) textViewLabName.text.toString() else ""
            val qrCodeBitmap =
                generateQRCode(name, lab, idCardNumber, emailID, contact, designation, division)
            Log.d("AT INTENT name", name)
            Log.d("AT INTENT lab", lab)
            Log.d("AT INTENT idCard", idCardNumber)
            Log.d("AT INTENT email", emailID)
            Log.d("AT INTENT contact", contact)
            Log.d("AT INTENT designation", designation)

            val qrCodeDialog = Dialog(this)

            qrCodeDialog.setContentView(R.layout.dialog_qr_code_display)
            val imageViewQRCodePopup =
                qrCodeDialog.findViewById<ImageView>(R.id.imageViewQRCodePopup)
            val buttonClosePopup = qrCodeDialog.findViewById<TextView>(R.id.buttonClosePopup)
            imageViewQRCodePopup.setImageBitmap(qrCodeBitmap)

            buttonClosePopup.setOnClickListener {
                qrCodeDialog.dismiss()
            }
            qrCodeDialog.show()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
        }
        dialogBuilder.show()
    }

    //13-09-2023
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_id_card, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_generate_qr -> {
                showDetailsSelectionPopup()
                true
            }

            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_scan_qr -> {
                startQRSacnner()
                true
            }
            R.id.menu_nfc -> {
                // Show NFC sharing dialog and start sharing
                nfcShareDialog = Dialog(this)
                nfcShareDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                nfcShareDialog.setContentView(R.layout.popup_nfc_share)
                nfcShareDialog.setCancelable(false)
                showNfcShareDialog()
                startNfcSharing()

                // Simulate successful NFC share after 3 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    hideNfcShareDialog()
                }, 3000)
                true
            }


            R.id.menu_request_detail_modification -> {
                val intent = Intent(this, UpdateUserActivity::class.java)
                intent.putExtra("full_name", textViewFullName.text.toString())
                intent.putExtra("designation", textViewDesignation.text.toString())
                intent.putExtra("division", textViewDivisionName.text.toString())
                intent.putExtra("lab", textViewLabName.text.toString())
                intent.putExtra("address", textViewAddress.text.toString())
                intent.putExtra("CardNumber", textViewIdCardNumber.text.toString())
                intent.putExtra("email", email)
                intent.putExtra("password", password)

                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            NFC_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startNfcSharing() // Retry if NFC permission granted
                } else {
                    // Handle NFC permission denied case
                }
            }
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val correctedPhotoUrl = photoPath
                    loadPhoto(correctedPhotoUrl)
                } else {
                    Log.e("PHOTOPATH", "ERROR $photoPath")
                }
            }
            // Add more cases if needed for other request codes
        }
    }


    private fun buildVcfData(
        name: String,
        lab: String,
        emailID: String,
        contact: String,
        designation: String,

        ): String {
        val builder = StringBuilder()
        builder.append("BEGIN:VCARD\n")
        builder.append("VERSION:3.0\n")
        builder.append("FN:$name\n")
        builder.append("ORG:$designation, $lab\n")  // Only organization name
        builder.append("TEL:$contact\n")  // Only phone number
        builder.append("EMAIL:$emailID\n")
        builder.append("END:VCARD")
        Log.d("NAME : ", name)
        Log.d("LAB : ", lab)
        Log.d("contact", contact)
        Log.d("email: ", emailID)
        Log.d("designation: ", designation)

        Log.d("BUILDER STRING VCF: ", builder.toString())
        return builder.toString()
    }

    private fun generateQRCode(
        name: String,
        lab: String,
        idCardNumber: String,
        emailID: String,
        contact: String,
        designation: String,
        division: String,
    ): Bitmap? {
        try {
            val qrData = buildVcfData(
                name,
                lab,
                emailID,
                contact,
                designation
            )
            val hints =
                mapOf<EncodeHintType, ErrorCorrectionLevel>(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 500, 500, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            Log.d("QRCode", "Width: $width, Height: $height") // Log dimensions
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            Log.d("QR DATA:", qrData)
            return getRoundedCornerBitmap(bmp, 30)
        } catch (e: WriterException) {
            e.printStackTrace()
            Log.e("QRCode", "QR Code generation error: ${e.message}")
        }
        return null
    }


    private fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val color = 0xff424242.toInt()
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private fun startQRSacnner() {
        val intent = Intent(this, QRScannerActivity::class.java)
        startActivity(intent)
    }


    private fun prepareNfcData(): String {
        val name = textViewFullName.text.toString()
        val lab = textViewLabName.text.toString()
        val emailID = intent.getStringExtra("emailId").toString()
        val contact = intent.getStringExtra("contact").toString()
        val designation = textViewDesignation.text.toString()
        val division = textViewDivisionName.text.toString()

        val builder = StringBuilder()
        builder.append("BEGIN:VCARD\n")
        builder.append("VERSION:3.0\n")
        builder.append("FN:$name\n")
        builder.append("ORG:$designation, $lab\n") // Only organization name
        builder.append("TEL:$contact\n") // Only phone number
        builder.append("EMAIL:$emailID\n")
        builder.append("END:VCARD")

        return builder.toString()
    }

    private fun startNfcSharing() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC detected", Toast.LENGTH_SHORT).show()
            return
        }
        if (!nfcAdapter.isEnabled) {
            // NFC is not enabled, prompt the user to enable it
            Toast.makeText(this, "Please enable NFC", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            return
        }

        val nfcData = prepareNfcData() // Prepare your data to share
        Log.d("NFC_DATA", nfcData)
        val mimeType = "text/vcard" // Replace with appropriate MIME type

        val nfcIntent = Intent(this, javaClass).apply {
            action = Intent.ACTION_SEND
            type = mimeType
            putExtra(Intent.EXTRA_TEXT, nfcData)
        }

        val nfcPendingIntent = PendingIntent.getActivity(
            this, 0, nfcIntent, PendingIntent.FLAG_MUTABLE
        )


        val readerCallback = NfcAdapter.ReaderCallback { tag ->
            // Create the NdefMessage to send
            val ndefMessage = NdefMessage(
                arrayOf(
                    NdefRecord.createMime(mimeType, nfcData.toByteArray(Charsets.UTF_8))
                )
            )
            // Return the message to send
            ndefMessage
        }

        // Start NFC reader mode
        nfcAdapter.enableReaderMode(
            this, readerCallback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B, null
        )

        // Enable foreground dispatch
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }


}