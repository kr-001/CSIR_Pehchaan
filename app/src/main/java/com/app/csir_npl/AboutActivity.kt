package com.app.csir_npl
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.app.csir_npl.databinding.ActivityAboutBinding
import java.util.*

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("language_pref", MODE_PRIVATE)

        // Initialize language switch state
        binding.languageSwitch.isChecked = sharedPreferences.getBoolean("isHindi", false)

        // Set up language switch listener
        binding.languageSwitch.setOnCheckedChangeListener { _, isChecked ->
            setLocale(if (isChecked) "hi" else "en")
        }

        // Set up LinkedIn clickable spans
        setupLinkedinClickableSpan()
    }

    private fun setupLinkedinClickableSpan() {
        val linkedinUrl1 = "https://www.linkedin.com/in/kumar-r-a53a03117/"
        val linkedinUrl2 = "https://www.linkedin.com/in/neeraj-bhanot-76638092/"

        val text1 = ""
        val spannableString1 = SpannableString(text1)
        val clickableSpan1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openLinkedInProfile(linkedinUrl1)
            }
        }
        spannableString1.setSpan(clickableSpan1, 0, text1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)



        val text2 = ""
        val spannableString2 = SpannableString(text2)
        val clickableSpan2 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openLinkedInProfile(linkedinUrl2)
            }
        }
        spannableString2.setSpan(clickableSpan2, 0, text2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


    }

    fun openLinkedInProfile(linkedinUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
        startActivity(intent)
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        val editor = sharedPreferences.edit()
        editor.putBoolean("isHindi", languageCode == "hi")
        editor.apply()

        recreate()
    }

    fun openLinkedInProfile(view: View) {
        val url = "https://www.linkedin.com/in/kumar-r-a53a03117/"
        val intent = Intent(Intent.ACTION_VIEW,  Uri.parse(url))
        startActivity(intent)
    }
    fun openLinkedInProfile2(view: View) {
        val url = "https://www.linkedin.com/in/neeraj-bhanot-76638092/"
        val intent = Intent(Intent.ACTION_VIEW,  Uri.parse(url))
        startActivity(intent)
    }


}
