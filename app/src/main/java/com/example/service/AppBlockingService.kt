package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class AppBlockingService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            // In a real app, I'd check against a list of restricted apps managed in the ViewModel or DataStore
            if (packageName == "com.facebook.katana") {
                // Block app
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                Toast.makeText(this, "App Blocked by StudyHub", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onInterrupt() {}
}
