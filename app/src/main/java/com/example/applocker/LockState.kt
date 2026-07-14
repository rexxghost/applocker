package com.example.applocker

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * In-process, in-memory state shared between LockAccessibilityService and
 * PinEntryActivity. Since the accessibility service and the app's activities
 * run in the same process by default, a simple singleton is enough here -
 * no need for SharedPreferences or a ContentProvider for this transient state.
 */
object LockState {

    private val unlockedUntil = ConcurrentHashMap<String, Long>()

    /** Guards against the accessibility service launching PinEntryActivity more than once. */
    val isPromptShowing = AtomicBoolean(false)

    fun markUnlocked(packageNames: List<String>) {
        val expiry = System.currentTimeMillis() + AppConstants.UNLOCK_GRACE_PERIOD_MS
        packageNames.forEach { unlockedUntil[it] = expiry }
    }

    fun isUnlocked(packageName: String): Boolean {
        val expiry = unlockedUntil[packageName] ?: return false
        return System.currentTimeMillis() < expiry
    }
}
