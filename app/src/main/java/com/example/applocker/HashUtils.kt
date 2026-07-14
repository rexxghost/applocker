package com.example.applocker

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Small helper for salting and hashing the PIN before it is persisted, so the
 * raw PIN is never stored in SharedPreferences.
 */
object HashUtils {

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String = sha256(pin + salt)
}
