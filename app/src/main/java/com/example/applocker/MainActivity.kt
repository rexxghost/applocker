package com.example.applocker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private lateinit var switchWhatsapp: SwitchCompat
    private lateinit var switchInstagram: SwitchCompat
    private lateinit var switchGallery: SwitchCompat
    private lateinit var switchChrome: SwitchCompat

    private lateinit var textAccessibilityStatus: TextView
    private lateinit var textPinWarning: TextView
    private lateinit var btnEnableAccessibility: Button
    private lateinit var btnSetPin: Button

    private val pinSetupLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshUi()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        switchWhatsapp = findViewById(R.id.switchWhatsapp)
        switchInstagram = findViewById(R.id.switchInstagram)
        switchGallery = findViewById(R.id.switchGallery)
        switchChrome = findViewById(R.id.switchChrome)

        textAccessibilityStatus = findViewById(R.id.textAccessibilityStatus)
        textPinWarning = findViewById(R.id.textPinWarning)
        btnEnableAccessibility = findViewById(R.id.btnEnableAccessibility)
        btnSetPin = findViewById(R.id.btnSetPin)

        btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnSetPin.setOnClickListener {
            pinSetupLauncher.launch(Intent(this, PinSetupActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun refreshUi() {
        val pinSet = PrefsHelper.isPinSet(this)
        val accessibilityEnabled = isAccessibilityServiceEnabled(this)

        textAccessibilityStatus.text = if (accessibilityEnabled) {
            getString(R.string.accessibility_enabled)
        } else {
            getString(R.string.accessibility_disabled)
        }

        btnSetPin.text = if (pinSet) getString(R.string.change_pin) else getString(R.string.set_pin)
        textPinWarning.visibility = if (pinSet) android.view.View.GONE else android.view.View.VISIBLE

        bindSwitch(switchWhatsapp, "whatsapp", pinSet)
        bindSwitch(switchInstagram, "instagram", pinSet)
        bindSwitch(switchGallery, "gallery", pinSet)
        bindSwitch(switchChrome, "chrome", pinSet)
    }

    private fun bindSwitch(switch: SwitchCompat, appId: String, enabled: Boolean) {
        // Clear any previous listener before programmatically setting state,
        // otherwise we'd immediately write the just-read value back to prefs.
        switch.setOnCheckedChangeListener(null)
        switch.isChecked = PrefsHelper.isAppLocked(this, appId)
        switch.isEnabled = enabled
        switch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setAppLocked(this, appId, isChecked)
        })
    }
}
