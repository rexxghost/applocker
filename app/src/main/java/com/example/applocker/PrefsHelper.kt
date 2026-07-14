package com.example.applocker

import android.content.Context
import android.content.SharedPreferences

/**
 * Central place for reading/writing the PIN hash and which apps are locked.
 */
object PrefsHelper {

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

    fun isPinSet(context: Context): Boolean {
        val p = prefs(context)
        return p.contains(AppConstants.KEY_PIN_HASH) && p.contains(AppConstants.KEY_PIN_SALT)
    }

    fun savePin(context: Context, pin: String) {
        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashPin(pin, salt)
        prefs(context).edit()
            .putString(AppConstants.KEY_PIN_SALT, salt)
            .putString(AppConstants.KEY_PIN_HASH, hash)
            .apply()
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val salt = p.getString(AppConstants.KEY_PIN_SALT, null) ?: return false
        val storedHash = p.getString(AppConstants.KEY_PIN_HASH, null) ?: return false
        return HashUtils.hashPin(pin, salt) == storedHash
    }

    /** Locked defaults to true once a PIN exists, so newly listed apps are protected by default. */
    fun isAppLocked(context: Context, appId: String): Boolean =
        prefs(context).getBoolean(AppConstants.KEY_LOCK_PREFIX + appId, true)

    fun setAppLocked(context: Context, appId: String, locked: Boolean) {
        prefs(context).edit()
            .putBoolean(AppConstants.KEY_LOCK_PREFIX + appId, locked)
            .apply()
    }
}
