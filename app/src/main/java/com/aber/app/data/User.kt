package com.aber.app.data

/** A registered account. We never store the password itself, only a salted hash. */
data class User(
    val firstName: String,
    val lastName: String,
    val identifier: String, // normalized email or mobile number
    val salt: String,
    val passwordHash: String
) {
    val fullName: String get() = "$firstName $lastName"
}

enum class AuthResult { OK, DUPLICATE, NOT_FOUND, WRONG_PASSWORD }
