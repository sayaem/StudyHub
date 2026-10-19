package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.notification.AdaptiveNotification
import com.example.domain.notification.NotificationCategory
import com.example.domain.notification.NotificationPriority
import com.example.receiver.StudyHubBroadcastReceiver
import com.example.util.AppLanguage
import com.example.util.PreferencesManager

object StudyHubNotificationHelper {

    private const val TAG = "StudyHubNotifications"

    const val CHANNEL_FOCUS = "studyhub_focus_channel"
    const val CHANNEL_REMINDERS = "studyhub_reminders_channel"
    const val CHANNEL_DEADLINES = "studyhub_deadlines_channel"

    const val NOTIFICATION_ID_FOCUS_COMPLETED = 1001
    const val NOTIFICATION_ID_SCHEDULED_REMINDER = 1003

    const val ACTION_FOCUS_COMPLETED_ALARM = "com.example.action.FOCUS_COMPLETED_ALARM"
    const val ACTION_ADAPTIVE_REMINDER_ALARM = "com.example.action.ADAPTIVE_REMINDER_ALARM"
    const val EXTRA_SESSION_SUBJECT = "extra_session_subject"
    const val EXTRA_SESSION_CHAPTER = "extra_session_chapter"
    const val EXTRA_SESSION_DURATION = "extra_session_duration"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // 1. Focus Sessions Channel (High importance with sound and vibration)
            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts and sound cues when a focus session or break ends"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            // 2. Study Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Study Reminders & Habits",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily study task reminders, spaced revisions, and habit cues"
                enableVibration(true)
            }

            // 3. Admission Deadlines Channel
            val deadlinesChannel = NotificationChannel(
                CHANNEL_DEADLINES,
                "Admission Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical admission application deadlines and exam date warnings"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(focusChannel)
            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(deadlinesChannel)
            Log.d(TAG, "Notification channels initialized successfully")
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showFocusCompletedNotification(
        context: Context,
        subjectName: String,
        chapterName: String,
        durationMinutes: Int
    ) {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot show focus notification: permission missing")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "FOCUS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_FOCUS_COMPLETED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎯 Focus Session Completed!"
        val content = if (subjectName.isNotBlank() && subjectName != "General Focus") {
            "Great job! You completed $durationMinutes minutes of deep focus on $subjectName ($chapterName). Take a short rest!"
        } else {
            "Great job! You completed $durationMinutes minutes of deep study focus. Take a well-deserved break!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID_FOCUS_COMPLETED, builder.build())
            Log.d(TAG, "Focus completion notification delivered to tray")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException showing focus notification", e)
        }
    }

    fun showTestReminderNotification(
        context: Context,
        title: String = "🔔 StudyHub Notification",
        message: String = "StudyHub notification engine is active. You will receive timely alerts for deadlines and study reminders."
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "NOTIFICATIONS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(1002, builder.build())
        } catch (_: SecurityException) {}
    }

    /**
     * Delivers an adaptive notification to the Android device notification tray.
     * Respects user quiet hours, channel categories, deduplication, and permissions.
     */
    fun deliverAdaptiveNotification(
        context: Context,
        notification: AdaptiveNotification,
        lang: AppLanguage
    ): Boolean {
        if (!hasNotificationPermission(context)) {
            Log.d(TAG, "Suppressed notification ${notification.id}: permission not granted")
            return false
        }

        val prefs = PreferencesManager(context)
        if (!prefs.isNotificationsEnabled || !prefs.isAdaptiveNotificationsEnabled) {
            Log.d(TAG, "Suppressed notification ${notification.id}: notifications disabled in settings")
            return false
        }

        // Check if recently delivered (deduplication within 12 hours)
        if (prefs.isNotificationDeliveredRecently(notification.id)) {
            Log.d(TAG, "Suppressed notification ${notification.id}: already delivered recently")
            return false
        }

        val channelId = when (notification.category) {
            NotificationCategory.ADMISSION -> CHANNEL_DEADLINES
            NotificationCategory.REVISION, NotificationCategory.PLAN_RECOVERY, NotificationCategory.ROUTINE, NotificationCategory.SESSION -> CHANNEL_REMINDERS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", notification.targetScreen.name)
        }

        val notifIntId = (notification.id.hashCode() and 0x7FFFFFFF)

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifIntId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = notification.getTitle(lang)
        val description = notification.getDescription(lang)

        val priority = when (notification.priority) {
            NotificationPriority.CRITICAL -> NotificationCompat.PRIORITY_MAX
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        return try {
            notificationManager.notify(notifIntId, builder.build())
            prefs.recordNotificationDelivered(notification.id)
            Log.d(TAG, "Delivered adaptive notification ${notification.id} ($title) to tray")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException delivering notification ${notification.id}", e)
            false
        }
    }

    /**
     * Cancels an obsolete notification from the device tray when task or revision is completed.
     */
    fun cancelNotification(context: Context, notificationId: String) {
        val notifIntId = (notificationId.hashCode() and 0x7FFFFFFF)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notifIntId)
        Log.d(TAG, "Cancelled notification with ID: $notificationId")
    }

    fun scheduleFocusCompletionAlarm(
        context: Context,
        triggerEpochMs: Long,
        subjectName: String,
        chapterName: String,
        durationMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, StudyHubBroadcastReceiver::class.java).apply {
            action = ACTION_FOCUS_COMPLETED_ALARM
            putExtra(EXTRA_SESSION_SUBJECT, subjectName)
            putExtra(EXTRA_SESSION_CHAPTER, chapterName)
            putExtra(EXTRA_SESSION_DURATION, durationMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_FOCUS_COMPLETED,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled focus alarm for epoch $triggerEpochMs")
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        }
    }

    fun cancelFocusCompletionAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyHubBroadcastReceiver::class.java).apply {
            action = ACTION_FOCUS_COMPLETED_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_FOCUS_COMPLETED,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled focus alarm")
        }
    }

    /**
     * Schedules periodic background evaluation of adaptive reminders.
     */
    fun scheduleAdaptivePeriodicAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyHubBroadcastReceiver::class.java).apply {
            action = ACTION_ADAPTIVE_REMINDER_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID_SCHEDULED_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Run evaluation every 6 hours
        val intervalMs = 6 * 60 * 60 * 1000L
        val triggerAt = System.currentTimeMillis() + intervalMs

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            intervalMs,
            pendingIntent
        )
        Log.d(TAG, "Scheduled periodic adaptive reminder alarm")
    }

    // Diagnostics data for debug inspection (no UI clutter)
    data class NotificationDiagnosticReport(
        val permissionGranted: Boolean,
        val engineEnabled: Boolean,
        val channelsActive: Boolean,
        val lastEvaluationTimestamp: Long,
        val quietHoursActive: Boolean
    )

    fun getDiagnosticReport(context: Context): NotificationDiagnosticReport {
        val prefs = PreferencesManager(context)
        val perm = hasNotificationPermission(context)
        val engineEnabled = prefs.isNotificationsEnabled && prefs.isAdaptiveNotificationsEnabled

        val now = System.currentTimeMillis()
        val cal = java.util.Calendar.getInstance()
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val qStart = prefs.quietHoursStart
        val qEnd = prefs.quietHoursEnd
        val isQuiet = if (qStart > qEnd) (hour >= qStart || hour < qEnd) else (hour in qStart until qEnd)

        return NotificationDiagnosticReport(
            permissionGranted = perm,
            engineEnabled = engineEnabled,
            channelsActive = true,
            lastEvaluationTimestamp = prefs.lastEngineEvaluationTimestamp,
            quietHoursActive = isQuiet
        ).also {
            Log.d(TAG, "Diagnostic Report: $it")
        }
    }
}
