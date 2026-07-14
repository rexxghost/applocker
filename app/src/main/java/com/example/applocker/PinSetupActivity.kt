package com.example.applocker

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PinSetupActivity : AppCompatActivity() {

    private enum class Step { VERIFY_OLD, ENTER_NEW, CONFIRM_NEW }

    private var step: Step = Step.ENTER_NEW
    private var newPin: String = ""

    private lateinit var titleText: TextView
    private lateinit var errorText: TextView
    private lateinit var pinInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_setup)

        titleText = findViewById(R.id.textSetupTitle)
        errorText = findViewById(R.id.textSetupError)
        pinInput = findViewById(R.id.editSetupPin)
        val btnNext = findViewById<Button>(R.id.btnSetupNext)

        step = if (PrefsHelper.isPinSet(this)) Step.VERIFY_OLD else Step.ENTER_NEW
        updateTitle()

        btnNext.setOnClickListener { handleNext() }

        pinInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleNext()
                true
            } else {
                false
            }
        }
    }

    private fun updateTitle() {
        titleText.text = when (step) {
            Step.VERIFY_OLD -> getString(R.string.setup_verify_old)
            Step.ENTER_NEW -> getString(R.string.setup_enter_new)
            Step.CONFIRM_NEW -> getString(R.string.setup_confirm_new)
        }
    }

    private fun handleNext() {
        val pin = pinInput.text.toString()
        if (pin.length != AppConstants.PIN_LENGTH) {
            showError(getString(R.string.error_pin_length))
            return
        }

        when (step) {
            Step.VERIFY_OLD -> {
                if (PrefsHelper.verifyPin(this, pin)) {
                    step = Step.ENTER_NEW
                    clearAndProceed()
                } else {
                    showError(getString(R.string.error_pin_wrong))
                }
            }

            Step.ENTER_NEW -> {
                newPin = pin
                step = Step.CONFIRM_NEW
                clearAndProceed()
            }

            Step.CONFIRM_NEW -> {
                if (pin == newPin) {
                    PrefsHelper.savePin(this, pin)
                    Toast.makeText(this, R.string.pin_saved, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    step = Step.ENTER_NEW
                    newPin = ""
                    showError(getString(R.string.error_pin_mismatch))
                    updateTitle()
                }
            }
        }
    }

    private fun clearAndProceed() {
        pinInput.text.clear()
        errorText.visibility = View.GONE
        updateTitle()
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        pinInput.text.clear()
    }
}
