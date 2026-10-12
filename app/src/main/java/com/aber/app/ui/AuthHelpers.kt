package com.aber.app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.InputType
import com.aber.app.R
import com.aber.app.util.Validators
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/** Small helpers shared by the Login and Sign-up screens. */

/** Switches the identifier field between "Mobile number" and "Email" (keyboard type + label). */
internal fun applyIdentifierMode(layout: TextInputLayout, field: TextInputEditText, useEmail: Boolean) {
    layout.hint = layout.context.getString(if (useEmail) R.string.email else R.string.mobile_number)
    field.inputType = if (useEmail) {
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    } else {
        InputType.TYPE_CLASS_PHONE
    }
    field.text?.clear()
    layout.error = null
}

/** Returns an error message for the identifier, or null when it is valid. */
internal fun identifierError(context: Context, value: String, asEmail: Boolean): String? = when {
    value.isEmpty() -> context.getString(R.string.err_required)
    asEmail && !Validators.isValidEmail(value) -> context.getString(R.string.err_email)
    !asEmail && !Validators.isValidMobile(value.replace(" ", "")) -> context.getString(R.string.err_mobile)
    else -> null
}

/** Opens the home screen and removes the login screens from the back stack. */
internal fun Activity.openHome() {
    startActivity(
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
    finish()
}
