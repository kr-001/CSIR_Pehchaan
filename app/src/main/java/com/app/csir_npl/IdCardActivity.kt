package  com.app.csir_npl
import android.annotation.SuppressLint
import android.app.Dialog
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
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.File

class IdCardActivity : AppCompatActivity() {
    private lateinit var imageViewPhoto: ImageView
    private lateinit var textViewTitle: TextView
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
    private lateinit var imageViewLogoRight : ImageView
    private lateinit var emergencyContact : TextView
    private  lateinit var textViewStatus: TextView
    private lateinit var qrCodeDialog: Dialog
    private lateinit var email : String
    private lateinit var password : String

    companion object {
        private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_id_card)


        imageViewPhoto = findViewById(R.id.imageViewPhoto)
        textViewTitle = findViewById(R.id.textViewTitle)
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
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        val title = intent.getStringExtra("title")
        val name = intent.getStringExtra("name")
        photoPath = intent.getStringExtra("photoPath").toString()

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
        Log.e("UserEmail" , "$email")
        Log.e("password" , "$password")
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
            textViewTitle.text = title
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

            if(emergency == "null"){
                emergencyContact.text = "Not Available"
            }else{
                emergencyContact.text = emergency
                emergencyContact.visibility = View.VISIBLE
            }

        } else {
            Log.e("IdCardActivity", "photoPath is null")
        }
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

            val emailID = if (checkboxEmail.isChecked) intent.getStringExtra("emailId").toString() else ""
            val contact = if (checkboxContact.isChecked) intent.getStringExtra("contact").toString() else ""
            val designation = if (checkboxDesignation.isChecked) textViewDesignation.text.toString() else ""
            val division = if(checkboxLabName.isChecked) textViewLabName.text.toString() else ""
            val qrCodeBitmap = generateQRCode(name, lab, idCardNumber, emailID, contact, designation, division)
            val qrCodeDialog = Dialog(this)

            qrCodeDialog.setContentView(R.layout.dialog_qr_code_display)
            val imageViewQRCodePopup = qrCodeDialog.findViewById<ImageView>(R.id.imageViewQRCodePopup)
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
        return when(item.itemId){
            R.id.menu_generate_qr -> {
                showDetailsSelectionPopup()
                true
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.menu_request_detail_modification -> {
                val intent = Intent(this , UpdateUserActivity::class.java)
                intent.putExtra("title", textViewTitle.text.toString())
                intent.putExtra("designation", textViewDesignation.text.toString())
                intent.putExtra("division", textViewDivisionName.text.toString())
                intent.putExtra("lab", textViewLabName.text.toString())
                intent.putExtra("address", textViewAddress.text.toString())
                intent.putExtra("CardNumber" , textViewIdCardNumber.text.toString())
                intent.putExtra("email" , email)
                intent.putExtra("password" , password)

                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val correctedPhotoUrl = photoPath
                    loadPhoto(correctedPhotoUrl)
                } else {
                    Log.e("PHOTOPATH", "ERROR $photoPath")
                }
            }
        }
    }



    private fun buildVcfData(
        name: String,
        lab: String,
        idCardNumber: String,
        emailID: String,
        contact: String,
        designation: String,
        division: String
    ): String {
        val builder = StringBuilder()
        builder.append("BEGIN:VCARD\n")
        builder.append("VERSION:3.0\n")
        builder.append("FN:$name\n")
        builder.append("ORG:$designation,$lab\n")
        builder.append("TEL:$contact\n")
        builder.append("EMAIL:$emailID\n")
        builder.append("END:VCARD")

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
                idCardNumber,
                emailID,
                contact,
                designation,
                division
            )
            val hints = mapOf<EncodeHintType, ErrorCorrectionLevel>(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
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




}
