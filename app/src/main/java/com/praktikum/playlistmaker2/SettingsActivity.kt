package com.praktikum.playlistmaker2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var themeSwitcher: SwitchMaterial
    private lateinit var shareAppButton: FrameLayout
    private lateinit var supportButton: FrameLayout
    private lateinit var userAgreementButton: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backButton = findViewById(R.id.button_back)
        themeSwitcher = findViewById(R.id.switch_theme)
        shareAppButton = findViewById(R.id.button_share)
        supportButton = findViewById(R.id.button_support)
        userAgreementButton = findViewById(R.id.button_agreement)

        val sharedPreferences = getSharedPreferences(App.PREFS_NAME, MODE_PRIVATE)
        themeSwitcher.isChecked = sharedPreferences.getBoolean(App.DARK_THEME_KEY, false)

        backButton.setOnClickListener {
            finish()
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            sharedPreferences.edit()
                .putBoolean(App.DARK_THEME_KEY, checked)
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        shareAppButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    getString(R.string.share_chooser_title)
                )
            )
        }

        supportButton.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.support_message))
            }
            startActivity(supportIntent)
        }

        userAgreementButton.setOnClickListener {
            val agreementIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.terms_url))
            )
            startActivity(agreementIntent)
        }
    }
}