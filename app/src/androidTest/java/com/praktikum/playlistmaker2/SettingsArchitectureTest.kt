package com.praktikum.playlistmaker2

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.switchmaterial.SwitchMaterial
import com.praktikum.playlistmaker2.presentation.SettingsActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsArchitectureTest {
    @Test
    fun themeChoiceSurvivesScreenRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val settings = Creator.createSettingsInteractor()
        val original = settings.getSettings().darkTheme
        val originalMode = AppCompatDelegate.getDefaultNightMode()
        try {
            ActivityScenario.launch(SettingsActivity::class.java).use { scenario ->
                scenario.onActivity {
                    it.findViewById<SwitchMaterial>(R.id.switch_theme).isChecked = !original
                }
                scenario.recreate()
                scenario.onActivity {
                    assertEquals(!original, it.findViewById<SwitchMaterial>(R.id.switch_theme).isChecked)
                    assertEquals(!original, Creator.createSettingsInteractor().getSettings().darkTheme)
                }
            }
        } finally {
            settings.setDarkTheme(original)
            instrumentation.runOnMainSync { AppCompatDelegate.setDefaultNightMode(originalMode) }
        }
    }
}
