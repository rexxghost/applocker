package com.example.applocker

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PinEntryActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_APP_ID = "extra_app_id"
        const val EXTRA_APP_LABEL = "extra_app_label"
    }

    private lateinit var appId: String
    private lateinit var pinInput: EditText
    private lateinit var titleText: TextView
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_entry)

        val incomingAppId = intent.getStringExtra(EXTRA_APP_ID)
        if (incomingAppId == null) {
            // Nothing to guard against, bail out safely.
            LockState.isPromptShowing.set(false)
            finish()
            return
        }
        appId = incomingAppId
        val label = intent.getStringExtra(EXTRA_APP_LABEL) ?: getString(R.string.app_name)

        titleText = findViewById(R.id.textLockTitle)
        errorText = findViewById(R.id.textError)
        pinInput = findViewById(R.id.editPin)
        val btnUnlock = findViewById<Button>(R.id.btnUnlock)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        titleText.text = getString(R.string.pin_entry_title, label)

        btnUnlock.setOnClickListener { attemptUnlock() }

        btnCancel.setOnClickListener {
            LockState.isPromptShowing.set(false)
            moveTaskToBack(true)
            finish()
        }

        pinInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock()
                true
            } else {
                false
            }
        }
    }

    private fun attemptUnlock() {
        val pin = pinInput.text.toString()
        if (pin.length != AppConstants.PIN_LENGTH) {
            showError(getString(R.string.error_pin_length))
            return
        }

        if (PrefsHelper.verifyPin(this, pin)) {
            val app = AppConstants.findById(appId)
            app?.let { LockState.markUnlocked(it.packageNames) }
            LockState.isPromptShowing.set(false)
            finish()
        } else {
            showError(getString(R.string.error_pin_wrong))
        }
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        pinInput.text.clear()
    }

    /**
     * Pressing back should not reveal the locked app underneath - send the
     * user home instead, same as tapping Cancel.
     */
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        LockState.isPromptShowing.set(false)
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        // In case the activity is destroyed without an explicit unlock/cancel
        // (e.g. system kills it), make sure we don't leave the flag stuck.
        LockState.isPromptShowing.set(false)
    }
}
