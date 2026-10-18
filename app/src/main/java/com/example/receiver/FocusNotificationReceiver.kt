package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.notification.StudyHubNotificationHelper
import com.example.util.PreferencesManager

class FocusNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == StudyHubNotificationHelper.ACTION_FOCUS_COMPLETED_ALARM) {
            val subjectName = intent.getStringExtra(StudyHubNotificationHelper.EXTRA_SESSION_SUBJECT) ?: "General Focus"
            val chapterName = intent.getStringExtra(StudyHubNotificationHelper.EXTRA_SESSION_CHAPTER) ?: "Study Session"
            val durationMinutes = intent.getIntExtra(StudyHubNotificationHelper.EXTRA_SESSION_DURATION, 25)

            // Deliver notification
            StudyHubNotificationHelper.showFocusCompletedNotification(
                context = context,
                subjectName = subjectName,
                chapterName = chapterName,
                durationMinutes = durationMinutes
            )

            // Vibrate
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }
            } catch (_: Exception) {}
        }
    }
}
