package com.aber.app.util

import android.util.Patterns

/**
 * Input checks used by the Login and Sign-up screens.
 * `object` in Kotlin = a singleton (like a Java class with only static methods).
 */
object Validators {

    // Philippine mobile numbers: 09XXXXXXXXX or +639XXXXXXXXX
    private val MOBILE_REGEX = Regex("^(09|\\+639)\\d{9}$")

    fun isValidEmail(value: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(value).matches()

    fun isValidMobile(value: String): Boolean = MOBILE_REGEX.matches(value)

    /** Password rule: at least 8 characters, with at least one letter and one digit. */
    fun isValidPassword(value: String): Boolean =
        value.length >= 8 && value.any { it.isLetter() } && value.any { it.isDigit() }

    /**
     * Makes the same account always look the same in storage:
     * emails are lowercased, "+63917..." becomes "0917...".
     */
    fun normalizeIdentifier(raw: String): String {
        val value = raw.trim().replace(" ", "")
        return when {
            value.contains("@") -> value.lowercase()
            value.startsWith("+63") -> "0" + value.substring(3)
            else -> value
        }
    }
}
