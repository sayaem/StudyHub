package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.example.ui.screens.BlockedAppActivity
import com.example.util.PreferencesManager

class StudyHubAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PreferencesManager
    private var lastBlockedPackage: String? = null
    private var lastBlockTimestamp: Long = 0L

    companion object {
        @Volatile
        var isServiceRunning: Boolean = false
            private set

        /**
         * Checks if the Accessibility Service is explicitly enabled by the user in Android Settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val expectedServiceName = "${context.packageName}/${StudyHubAccessibilityService::class.java.name}"
            val shortExpectedServiceName = "${context.packageName}/.service.StudyHubAccessibilityService"

            val enabledServicesSetting = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServicesSetting)

            while (colonSplitter.hasNext()) {
                val componentNameString = colonSplitter.next()
                if (componentNameString.equals(expectedServiceName, ignoreCase = true) ||
                    componentNameString.equals(shortExpectedServiceName, ignoreCase = true) ||
                    componentNameString.contains("StudyHubAccessibilityService", ignoreCase = true)
                ) {
                    return true
                }
            }
            return isServiceRunning
        }

        // Whitelisted packages that must NEVER be blocked for safety and system integrity
        val SYSTEM_WHITELIST = setOf(
            "com.android.settings",
            "com.android.systemui",
            "com.android.phone",
            "com.google.android.dialer",
            "com.samsung.android.dialer",
            "com.android.server.telecom",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.android.emergency",
            "com.google.android.apps.safetyhub"
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        prefs = PreferencesManager(applicationContext)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (packageName.isBlank()) return

        // 1. Safety check: Never block StudyHub itself or critical system services
        if (packageName == applicationContext.packageName) return
        if (SYSTEM_WHITELIST.contains(packageName) || packageName.startsWith("com.android.systemui")) {
            return
        }

        // 2. Check if Focus Blocker is enabled and Focus Session is actively RUNNING
        if (!::prefs.isInitialized) {
            prefs = PreferencesManager(applicationContext)
        }

        if (!prefs.isDistractionBlockerEnabled) return
        val isTimerRunning = prefs.getTimerRunning()
        val isTimerPaused = prefs.getTimerPaused()

        // Distraction blocker is strictly enforced ONLY when session is actively running (not paused, not idle)
        if (!isTimerRunning || isTimerPaused) {
            return
        }

        // 3. Check if foreground package is in user's blocked apps list
        val blockedApps = prefs.blockedApps
        if (blockedApps.isEmpty() || !blockedApps.contains(packageName)) {
            return
        }

        // 4. Debounce to prevent redirect loops or activity launch storms (minimum 1.5s between blocks)
        val now = System.currentTimeMillis()
        if (packageName == lastBlockedPackage && (now - lastBlockTimestamp) < 1500L) {
            return
        }
        lastBlockedPackage = packageName
        lastBlockTimestamp = now

        // Record interruption count
        prefs.recordInterruption(packageName)

        // 5. Enforce restriction: Return user gracefully to StudyHub blocking screen
        launchBlockedScreen(packageName)
    }

    private fun launchBlockedScreen(blockedPackage: String) {
        try {
            val intent = Intent(applicationContext, BlockedAppActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(BlockedAppActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
            }
            startActivity(intent)
        } catch (_: Exception) {}
    }

    override fun onInterrupt() {
        // Nothing to clean up
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }
}
