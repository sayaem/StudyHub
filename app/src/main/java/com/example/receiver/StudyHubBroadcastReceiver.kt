package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.data.local.StudyHubDatabase
import com.example.domain.notification.AdaptiveNotificationEngine
import com.example.notification.StudyHubNotificationHelper
import com.example.util.AppLanguage
import com.example.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class StudyHubBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("StudyHubReceiver", "Received broadcast: $action")

        when (action) {
            StudyHubNotificationHelper.ACTION_FOCUS_COMPLETED_ALARM -> {
                val subjectName = intent.getStringExtra(StudyHubNotificationHelper.EXTRA_SESSION_SUBJECT) ?: "General Focus"
                val chapterName = intent.getStringExtra(StudyHubNotificationHelper.EXTRA_SESSION_CHAPTER) ?: "Study Session"
                val durationMinutes = intent.getIntExtra(StudyHubNotificationHelper.EXTRA_SESSION_DURATION, 25)

                // Deliver notification to tray
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

            StudyHubNotificationHelper.ACTION_ADAPTIVE_REMINDER_ALARM -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val prefs = PreferencesManager(context)
                        prefs.lastEngineEvaluationTimestamp = System.currentTimeMillis()

                        val db = StudyHubDatabase.getDatabase(context, this)
                        val admissions = db.admissionDao().getAllTargets().firstOrNull() ?: emptyList()
                        val chapters = db.chapterDao().getAllChapters().firstOrNull() ?: emptyList()
                        val tasks = db.plannerTaskDao().getAllTasks().firstOrNull() ?: emptyList()
                        val revisions = db.revisionItemDao().getAllRevisions().firstOrNull() ?: emptyList()
                        val mistakes = db.mistakeDao().getAllMistakes().firstOrNull() ?: emptyList()

                        val engine = AdaptiveNotificationEngine(prefs)
                        val notifications = engine.evaluateNotifications(
                            admissions = admissions,
                            chapters = chapters,
                            plannerTasks = tasks,
                            revisions = revisions,
                            mistakes = mistakes
                        )

                        val lang = if (prefs.language == "bn") AppLanguage.BANGLA else AppLanguage.ENGLISH
                        for (notif in notifications) {
                            StudyHubNotificationHelper.deliverAdaptiveNotification(context, notif, lang)
                        }
                    } catch (e: Exception) {
                        Log.e("StudyHubReceiver", "Error evaluating adaptive notifications in background", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("StudyHubReceiver", "Device boot completed, re-initializing notification alarms")
                StudyHubNotificationHelper.initNotificationChannels(context)
                StudyHubNotificationHelper.scheduleAdaptivePeriodicAlarm(context)
            }
        }
    }
}
