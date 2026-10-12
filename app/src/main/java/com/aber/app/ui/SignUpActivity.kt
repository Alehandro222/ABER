package com.aber.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aber.app.R
import com.aber.app.data.AuthResult
import com.aber.app.data.UserStore
import com.aber.app.databinding.ActivitySignupBinding
import com.aber.app.util.Validators

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var store: UserStore

    // false = sign up with mobile number (first Figma screen), true = with email
    private var useEmail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        store = UserStore(this)
        applyMode()

        binding.btnSwitchMode.setOnClickListener {
            useEmail = !useEmail
            applyMode()
        }
        binding.cbTerms.setOnCheckedChangeListener { _, checked ->
            if (checked) binding.tvTermsError.visibility = View.GONE
        }
        binding.btnSignup.setOnClickListener { attemptSignUp() }
        binding.tvLogin.setOnClickListener { finish() } // back to the login screen
    }

    private fun applyMode() {
        applyIdentifierMode(binding.tilIdentifier, binding.etIdentifier, useEmail)
        binding.btnSwitchMode.setText(if (useEmail) R.string.sign_up_with_mobile else R.string.sign_up_with_email)
    }

    private fun attemptSignUp() {
        // Clear old errors
        binding.tilFirstName.error = null
        binding.tilLastName.error = null
        binding.tilIdentifier.error = null
        binding.tilPassword.error = null
        binding.tilConfirm.error = null
        binding.tvTermsError.visibility = View.GONE

        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val identifier = binding.etIdentifier.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirm.text.toString()

        // Check every field so the user sees all problems at once.
        var allValid = true
        if (firstName.isEmpty()) {
            binding.tilFirstName.error = getString(R.string.err_required)
            allValid = false
        }
        if (lastName.isEmpty()) {
            binding.tilLastName.error = getString(R.string.err_required)
            allValid = false
        }
        val idError = identifierError(this, identifier, useEmail)
        if (idError != null) {
            binding.tilIdentifier.error = idError
            allValid = false
        }
        if (!Validators.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.err_password_rule)
            allValid = false
        }
        if (confirm != password) {
            binding.tilConfirm.error = getString(R.string.err_password_mismatch)
            allValid = false
        }
        if (!binding.cbTerms.isChecked) {
            binding.tvTermsError.visibility = View.VISIBLE
            allValid = false
        }
        if (!allValid) return

        val result = store.register(firstName, lastName, identifier, password)
        if (result == AuthResult.OK) {
            Toast.makeText(this, R.string.welcome_new, Toast.LENGTH_SHORT).show()
            openHome()
        } else if (result == AuthResult.DUPLICATE) {
            binding.tilIdentifier.error = getString(R.string.err_duplicate)
        }
    }
}
