package com.praktikum.playlistmaker2.presentation

import com.praktikum.playlistmaker2.R
import com.praktikum.playlistmaker2.Creator

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var themeSwitcher: SwitchMaterial
    private lateinit var shareAppButton: FrameLayout
    private lateinit var supportButton: FrameLayout
    private lateinit var userAgreementButton: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_settings)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root)) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }

        backButton = findViewById(R.id.button_back)
        themeSwitcher = findViewById(R.id.switch_theme)
        shareAppButton = findViewById(R.id.button_share)
        supportButton = findViewById(R.id.button_support)
        userAgreementButton = findViewById(R.id.button_agreement)

        val settingsInteractor = Creator.createSettingsInteractor()
        val sharingInteractor = Creator.createSharingInteractor()
        themeSwitcher.isChecked = settingsInteractor.getSettings().darkTheme

        backButton.setOnClickListener {
            finish()
        }

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            settingsInteractor.setDarkTheme(checked)

            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        shareAppButton.setOnClickListener {
            sharingInteractor.shareApp()
        }

        supportButton.setOnClickListener {
            sharingInteractor.contactSupport()
        }

        userAgreementButton.setOnClickListener {
            sharingInteractor.openTerms()
        }
    }
}
