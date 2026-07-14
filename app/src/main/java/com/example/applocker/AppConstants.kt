package com.example.applocker

/**
 * A single lockable entry shown in the UI. Some entries (like Gallery) map to
 * more than one possible package name since different manufacturers ship
 * different gallery apps.
 */
data class LockableApp(
    val id: String,
    val displayName: String,
    val packageNames: List<String>
)

object AppConstants {

    const val PIN_LENGTH = 4

    /** How long (ms) an app stays "unlocked" after a correct PIN before it can be prompted again. */
    const val UNLOCK_GRACE_PERIOD_MS = 3000L

    const val PREFS_NAME = "app_locker_prefs"

    const val KEY_PIN_HASH = "pin_hash"
    const val KEY_PIN_SALT = "pin_salt"
    const val KEY_LOCK_PREFIX = "lock_"

    val LOCKABLE_APPS: List<LockableApp> = listOf(
        LockableApp(
            id = "whatsapp",
            displayName = "WhatsApp",
            packageNames = listOf("com.whatsapp")
        ),
        LockableApp(
            id = "instagram",
            displayName = "Instagram",
            packageNames = listOf("com.instagram.android")
        ),
        LockableApp(
            id = "gallery",
            displayName = "Gallery",
            packageNames = listOf(
                "com.google.android.apps.photos",
                "com.android.gallery3d",
                "com.sec.android.gallery3d",
                "com.miui.gallery",
                "com.oneplus.gallery"
            )
        ),
        LockableApp(
            id = "chrome",
            displayName = "Chrome",
            packageNames = listOf("com.android.chrome")
        )
    )

    /** Fast lookup: package name -> LockableApp */
    fun findByPackage(packageName: String): LockableApp? =
        LOCKABLE_APPS.firstOrNull { it.packageNames.contains(packageName) }

    fun findById(id: String): LockableApp? =
        LOCKABLE_APPS.firstOrNull { it.id == id }
}
