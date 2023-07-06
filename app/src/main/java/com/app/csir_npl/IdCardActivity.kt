package  com.app.csir_npl
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
    private lateinit var checkboxEmail: CheckBox
    private lateinit var checkboxContact: CheckBox
    private lateinit var checkboxStatus: CheckBox
    private lateinit var checkboxAutho: CheckBox
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
        checkboxEmail = findViewById(R.id.checkboxEmail)
        checkboxContact = findViewById(R.id.checkboxContact)
        checkboxStatus = findViewById(R.id.checkboxStatus)
        checkboxAutho = findViewById(R.id.checkboxAutho)
        buttonShareVcf = findViewById(R.id.buttonShareVcf)
        buttonGenerateQR = findViewById(R.id.buttonGenerateQR)
        imageViewQRCode = findViewById(R.id.imageViewQRCode)

        val title = intent.getStringExtra("title")
        val name = intent.getStringExtra("name")
        photoPath = intent.getStringExtra("photoPath").toString()

        val photoUrl = photoPath
        val correctedPhotoUrl = photoUrl.replace("\\", "/")
        val designation = intent.getStringExtra("designation")
        val division = intent.getStringExtra("division")
        val cityState = intent.getStringExtra("cityState")
        val lab = intent.getStringExtra("lab")
        val idCardNumber = intent.getStringExtra("idCardNumber")
        val emailId = intent.getStringExtra("emailId")
        val contact = intent.getStringExtra("contact")
        val status = intent.getStringExtra("status")
        val autho = intent.getStringExtra("autho")

        if (name != null && photoPath != null) {
            Log.i("PhotoPath: ", "$correctedPhotoUrl")
            loadPhoto(correctedPhotoUrl)
            textViewTitle.text = title
            textViewFullName.text = name
            textViewDesignation.text = designation
            textViewDivisionName.text = division
            textViewLabName.text = lab
            textViewCityState.text = cityState
            textViewIdCardNumber.text = idCardNumber

            buttonShareVcf.setOnClickListener {
                val name = textViewFullName.text.toString()
                val lab = textViewLabName.text.toString()
                val idCardNumber = textViewIdCardNumber.text.toString()

                val emailSelected = checkboxEmail.isChecked
                val contactSelected = checkboxContact.isChecked
                val statusSelected = checkboxStatus.isChecked
                val authoSelected = checkboxAutho.isChecked

                shareVcfFile(name, lab, idCardNumber, emailId, contact, status, autho,
                    emailSelected, contactSelected, statusSelected, authoSelected)
            }

            buttonGenerateQR.setOnClickListener {
                val name = textViewFullName.text.toString()
                val lab = textViewLabName.text.toString()
                val idCardNumber = textViewIdCardNumber.text.toString()

                val emailSelected = checkboxEmail.isChecked
                val contactSelected = checkboxContact.isChecked
                val statusSelected = checkboxStatus.isChecked
                val authoSelected = checkboxAutho.isChecked

                val emailValue = if (emailSelected) emailId else ""
                val contactValue = if (contactSelected) contact else ""
                val statusValue = if (statusSelected) status else ""
                val authoValue = if (authoSelected) autho else ""

                generateQRCode(name, lab, idCardNumber, emailValue!!, contactValue!!, statusValue!!, authoValue!!,
                    emailSelected, contactSelected, statusSelected, authoSelected)
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

    private fun shareVcfFile(
        name: String,
        lab: String,
        idCardNumber: String,
        emailID: String?,
        contact: String?,
        status: String?,
        autho: String?,
        emailSelected: Boolean,
        contactSelected: Boolean,
        statusSelected: Boolean,
        authoSelected: Boolean
    ) {
        val vcfFile = File(cacheDir, "contact.vcf")
        val vcfData = buildVcfData(
            name,
            lab,
            idCardNumber,
            emailID,
            contact,
            status,
            autho,
            emailSelected,
            contactSelected,
            statusSelected,
            authoSelected
        )
        vcfFile.writeText(vcfData)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/x-vcard"
        shareIntent.putExtra(Intent.EXTRA_STREAM, vcfFile)
        startActivity(Intent.createChooser(shareIntent, "Share Contact"))
    }

    private fun buildVcfData(
        name: String,
        lab: String,
        idCardNumber: String,
        emailID: String?,
        contact: String?,
        status: String?,
        autho: String?,
        emailSelected: Boolean,
        contactSelected: Boolean,
        statusSelected: Boolean,
        authoSelected: Boolean
    ): String {
        val builder = StringBuilder()
        builder.append("BEGIN:VCARD\n")
        builder.append("VERSION:3.0\n")
        builder.append("FN:$name\n")

        if (emailSelected && emailID != null) {
            builder.append("EMAIL:$emailID\n")
        }

        if (contactSelected && contact != null) {
            builder.append("CONTACT:$contact\n")
        }

        if (statusSelected && status != null) {
            builder.append("STATUS:$status\n")
        }

        if (authoSelected && autho != null) {
            builder.append("AUTHO:$autho\n")
        }

        builder.append("LAB:$lab\n")
        builder.append("ID:$idCardNumber\n")
        builder.append("END:VCARD")

        return builder.toString()
    }

    private fun generateQRCode(
        name: String,
        lab: String,
        idCardNumber: String,
        emailID: String,
        contact: String,
        status: String,
        autho: String,
        emailSelected: Boolean,
        contactSelected: Boolean,
        statusSelected: Boolean,
        authoSelected: Boolean
    ) {
        try {
            val qrData = buildVcfData(
                name,
                lab,
                idCardNumber,
                emailID,
                contact,
                status,
                autho,
                emailSelected,
                contactSelected,
                statusSelected,
                authoSelected
            )

            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap =
                barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 500, 500)

            imageViewQRCode.setImageBitmap(bitmap)
            imageViewQRCode.visibility = ImageView.VISIBLE
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
