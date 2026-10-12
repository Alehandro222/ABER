package com.aber.app.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.aber.app.util.Validators
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Local-only account storage (no server). Accounts live in SharedPreferences as JSON.
 * Passwords are stored as SHA-256(salt + password), never in plain text.
 * (A real app would verify passwords on a server with a slow hash such as bcrypt/Argon2.)
 */
class UserStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    // ---------- Accounts ----------

    fun register(firstName: String, lastName: String, rawIdentifier: String, password: String): AuthResult {
        val identifier = Validators.normalizeIdentifier(rawIdentifier)
        val users = loadUsers()
        if (users.any { it.identifier == identifier }) return AuthResult.DUPLICATE

        val salt = newSalt()
        val user = User(firstName.trim(), lastName.trim(), identifier, salt, hash(password, salt))
        users.add(user)
        saveUsers(users)
        setSession(identifier)
        return AuthResult.OK
    }

    fun login(rawIdentifier: String, password: String): AuthResult {
        val identifier = Validators.normalizeIdentifier(rawIdentifier)
        val user = loadUsers().firstOrNull { it.identifier == identifier } ?: return AuthResult.NOT_FOUND
        if (hash(password, user.salt) != user.passwordHash) return AuthResult.WRONG_PASSWORD
        setSession(identifier)
        return AuthResult.OK
    }

    /** Demo-only reset: a real app would first verify the person with an SMS/email code. */
    fun resetPassword(rawIdentifier: String, newPassword: String): Boolean {
        val identifier = Validators.normalizeIdentifier(rawIdentifier)
        val users = loadUsers()
        val index = users.indexOfFirst { it.identifier == identifier }
        if (index < 0) return false
        val salt = newSalt()
        users[index] = users[index].copy(salt = salt, passwordHash = hash(newPassword, salt))
        saveUsers(users)
        return true
    }

    // ---------- Session (who is logged in) ----------

    fun currentUser(): User? {
        val id = prefs.getString(KEY_SESSION, null) ?: return null
        return loadUsers().firstOrNull { it.identifier == id }
    }

    fun logout() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private fun setSession(identifier: String) {
        prefs.edit().putString(KEY_SESSION, identifier).apply()
    }

    // ---------- Storage helpers ----------

    private fun loadUsers(): MutableList<User> {
        val raw = prefs.getString(KEY_USERS, null) ?: return mutableListOf()
        val array = JSONArray(raw)
        val users = mutableListOf<User>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            users.add(
                User(
                    firstName = o.getString("firstName"),
                    lastName = o.getString("lastName"),
                    identifier = o.getString("identifier"),
                    salt = o.getString("salt"),
                    passwordHash = o.getString("passwordHash")
                )
            )
        }
        return users
    }

    private fun saveUsers(users: List<User>) {
        val array = JSONArray()
        for (u in users) {
            array.put(
                JSONObject()
                    .put("firstName", u.firstName)
                    .put("lastName", u.lastName)
                    .put("identifier", u.identifier)
                    .put("salt", u.salt)
                    .put("passwordHash", u.passwordHash)
            )
        }
        prefs.edit().putString(KEY_USERS, array.toString()).apply()
    }

    private fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private companion object {
        const val FILE_NAME = "aber_accounts"
        const val KEY_USERS = "users"
        const val KEY_SESSION = "session_identifier"
    }
}
