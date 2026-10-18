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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.receiver.FocusNotificationReceiver

object StudyHubNotificationHelper {

    const val CHANNEL_FOCUS = "studyhub_focus_channel"
    const val CHANNEL_REMINDERS = "studyhub_reminders_channel"
    const val CHANNEL_DEADLINES = "studyhub_deadlines_channel"

    const val NOTIFICATION_ID_FOCUS_COMPLETED = 1001
    const val NOTIFICATION_ID_REMINDER_TEST = 1002
    const val NOTIFICATION_ID_SCHEDULED_REMINDER = 1003

    const val ACTION_FOCUS_COMPLETED_ALARM = "com.example.action.FOCUS_COMPLETED_ALARM"
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
        if (!hasNotificationPermission(context)) return

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
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
        } catch (_: SecurityException) {
            // Handled safely
        }
    }

    fun showTestReminderNotification(
        context: Context,
        title: String = "🔔 StudyHub System Notification",
        message: String = "System push notifications are active! You will receive timely alerts for completed focus sessions and study deadlines."
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "FOCUS")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_REMINDER_TEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(NOTIFICATION_ID_REMINDER_TEST, builder.build())
        } catch (_: SecurityException) {}
    }

    fun scheduleFocusCompletionAlarm(
        context: Context,
        triggerEpochMs: Long,
        subjectName: String,
        chapterName: String,
        durationMinutes: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, FocusNotificationReceiver::class.java).apply {
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
        } catch (_: SecurityException) {
            // Fallback to inexact if exact alarm permission is restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        }
    }

    fun cancelFocusCompletionAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, FocusNotificationReceiver::class.java).apply {
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
        }
    }
}
