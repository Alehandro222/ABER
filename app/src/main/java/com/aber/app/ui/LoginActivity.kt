package com.aber.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.aber.app.R
import com.aber.app.data.AuthResult
import com.aber.app.data.UserStore
import com.aber.app.databinding.ActivityLoginBinding
import com.aber.app.databinding.DialogResetPasswordBinding
import com.aber.app.util.Validators
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var store: UserStore

    // false = log in with mobile number (the first screen in the Figma), true = with email
    private var useEmail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = UserStore(this)

        // Already logged in? Skip straight to the home screen.
        if (store.currentUser() != null) {
            openHome()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyMode()

        binding.btnSwitchMode.setOnClickListener {
            useEmail = !useEmail
            applyMode()
        }
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.tvForgot.setOnClickListener { showResetDialog() }
    }

    private fun applyMode() {
        applyIdentifierMode(binding.tilIdentifier, binding.etIdentifier, useEmail)
        binding.btnSwitchMode.setText(if (useEmail) R.string.login_with_mobile else R.string.login_with_email)
    }

    private fun attemptLogin() {
        binding.tilIdentifier.error = null
        binding.tilPassword.error = null

        val identifier = binding.etIdentifier.text.toString().trim()
        val password = binding.etPassword.text.toString()

        val idError = identifierError(this, identifier, useEmail)
        if (idError != null) {
            binding.tilIdentifier.error = idError
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.err_required)
            return
        }

        when (store.login(identifier, password)) {
            AuthResult.OK -> openHome()
            AuthResult.NOT_FOUND -> binding.tilIdentifier.error = getString(R.string.err_no_account)
            AuthResult.WRONG_PASSWORD -> binding.tilPassword.error = getString(R.string.err_wrong_password)
            AuthResult.DUPLICATE -> Unit // only happens when registering
        }
    }

    /** "Forgot Password?" - demo reset that stays on the phone (no SMS/email server). */
    private fun showResetDialog() {
        val dialogBinding = DialogResetPasswordBinding.inflate(layoutInflater)
        val dialog: AlertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reset_title)
            .setMessage(R.string.reset_message)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.reset_confirm, null) // set below so the dialog stays open on errors
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                dialogBinding.tilResetIdentifier.error = null
                dialogBinding.tilResetPassword.error = null

                val identifier = dialogBinding.etResetIdentifier.text.toString().trim()
                val newPassword = dialogBinding.etResetPassword.text.toString()

                val idError = identifierError(this, identifier, identifier.contains("@"))
                if (idError != null) {
                    dialogBinding.tilResetIdentifier.error = idError
                    return@setOnClickListener
                }
                if (!Validators.isValidPassword(newPassword)) {
                    dialogBinding.tilResetPassword.error = getString(R.string.err_password_rule)
                    return@setOnClickListener
                }
                if (store.resetPassword(identifier, newPassword)) {
                    dialog.dismiss()
                    Toast.makeText(this, R.string.reset_done, Toast.LENGTH_LONG).show()
                } else {
                    dialogBinding.tilResetIdentifier.error = getString(R.string.err_no_account)
                }
            }
        }
        dialog.show()
    }
}
