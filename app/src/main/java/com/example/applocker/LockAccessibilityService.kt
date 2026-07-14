package com.example.applocker

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

/**
 * Watches for TYPE_WINDOW_STATE_CHANGED events (fired whenever a new window,
 * i.e. a new foreground app/activity, appears) and, if that window belongs to
 * one of the locked apps and it hasn't been unlocked recently, brings up
 * PinEntryActivity on top of it.
 */
class LockAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        // Ignore events coming from our own app (e.g. the PIN screen itself).
        if (pkg == packageName) return

        val app = AppConstants.findByPackage(pkg) ?: return

        if (!PrefsHelper.isPinSet(this)) return
        if (!PrefsHelper.isAppLocked(this, app.id)) return
        if (LockState.isUnlocked(pkg)) return

        // Avoid stacking multiple PIN screens if several window events fire in a row.
        if (!LockState.isPromptShowing.compareAndSet(false, true)) return

        val intent = Intent(this, PinEntryActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(PinEntryActivity.EXTRA_APP_ID, app.id)
            putExtra(PinEntryActivity.EXTRA_APP_LABEL, app.displayName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // No-op: nothing to clean up when the system interrupts the service.
    }
}
