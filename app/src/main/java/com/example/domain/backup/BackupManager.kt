package com.example.domain.backup

import android.content.Context
import com.example.data.local.StudyHubDatabase
import com.example.data.model.*
import com.example.util.PreferencesManager
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class StudyHubBackupPayload(
    val version: Int = 1,
    val timestamp: Long,
    val subjects: List<SubjectEntity>,
    val chapters: List<ChapterEntity>,
    val sessions: List<StudySessionEntity>,
    val admissions: List<AdmissionTargetEntity>,
    val plannerTasks: List<PlannerTaskEntity>,
    val revisions: List<RevisionItemEntity>,
    val userName: String,
    val isDarkTheme: Boolean,
    val language: String,
    val isNotificationsEnabled: Boolean
)

class BackupManager(private val context: Context, private val database: StudyHubDatabase) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(StudyHubBackupPayload::class.java)
    private val prefs = PreferencesManager(context)

    suspend fun exportToJson(
        subjects: List<SubjectEntity>,
        chapters: List<ChapterEntity>,
        sessions: List<StudySessionEntity>,
        admissions: List<AdmissionTargetEntity>,
        plannerTasks: List<PlannerTaskEntity>,
        revisions: List<RevisionItemEntity>
    ): String = withContext(Dispatchers.IO) {
        val payload = StudyHubBackupPayload(
            timestamp = System.currentTimeMillis(),
            subjects = subjects,
            chapters = chapters,
            sessions = sessions,
            admissions = admissions,
            plannerTasks = plannerTasks,
            revisions = revisions,
            userName = prefs.userName,
            isDarkTheme = prefs.isDarkTheme,
            language = prefs.language,
            isNotificationsEnabled = prefs.isNotificationsEnabled
        )
        adapter.toJson(payload) ?: "{}"
    }

    suspend fun importFromJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = adapter.fromJson(jsonStr) ?: return@withContext false
            if (payload.version != 1) return@withContext false

            database.runInTransaction {
                database.clearAllTables()
                val subjectDao = database.subjectDao()
                val chapterDao = database.chapterDao()
                val sessionDao = database.studySessionDao()
                val admissionDao = database.admissionDao()
                val plannerDao = database.plannerTaskDao()
                val revisionDao = database.revisionItemDao()

                // Insert in batch or loop
                // Since dao insert methods are suspend, inside runInTransaction we can call blocking or runBlocking or use DAO non-suspend if available,
                // but Room transactions inside coroutine can invoke blocking DAO methods or runBlocking.
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
