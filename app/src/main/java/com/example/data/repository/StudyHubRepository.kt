package com.example.data.repository

import com.example.data.local.StudyHubDatabase
import com.example.data.local.populateInitialData
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudyHubRepository(private val database: StudyHubDatabase) {

    val allSubjects: Flow<List<SubjectEntity>> = database.subjectDao().getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = database.chapterDao().getAllChapters()
    val revisionChapters: Flow<List<ChapterEntity>> = database.chapterDao().getRevisionChapters()
    val allSessions: Flow<List<StudySessionEntity>> = database.studySessionDao().getAllSessions()
    val allAdmissions: Flow<List<AdmissionTargetEntity>> = database.admissionDao().getAllTargets()

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        database.chapterDao().getChaptersBySubject(subjectId)

    fun getSessionsSince(sinceMs: Long): Flow<List<StudySessionEntity>> =
        database.studySessionDao().getSessionsSince(sinceMs)

    suspend fun insertSubject(subject: SubjectEntity): Long = withContext(Dispatchers.IO) {
        database.subjectDao().insertSubject(subject)
    }

    suspend fun deleteSubject(subjectId: Long) = withContext(Dispatchers.IO) {
        database.subjectDao().deleteSubjectById(subjectId)
    }

    suspend fun insertChapter(chapter: ChapterEntity): Long = withContext(Dispatchers.IO) {
        database.chapterDao().insertChapter(chapter)
    }

    suspend fun updateChapter(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        database.chapterDao().updateChapter(chapter)
    }

    suspend fun deleteChapter(chapterId: Long) = withContext(Dispatchers.IO) {
        database.chapterDao().deleteChapterById(chapterId)
    }

    suspend fun cycleChapterStatus(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        val nextStatus = when (chapter.status) {
            "NOT_STARTED" -> "IN_PROGRESS"
            "IN_PROGRESS" -> "COMPLETED"
            "COMPLETED" -> "NOT_STARTED"
            else -> "IN_PROGRESS"
        }
        database.chapterDao().updateStatus(chapter.id, nextStatus)
    }

    suspend fun toggleRevisionNeeded(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        val newNeeded = !chapter.isRevisionNeeded
        database.chapterDao().updateRevision(
            id = chapter.id,
            needed = newNeeded,
            incrementRevision = 0,
            lastRevised = chapter.lastRevisedAt
        )
    }

    suspend fun markChapterRevised(chapter: ChapterEntity) = withContext(Dispatchers.IO) {
        database.chapterDao().updateRevision(
            id = chapter.id,
            needed = false,
            incrementRevision = 1,
            lastRevised = System.currentTimeMillis()
        )
    }

    suspend fun logSession(session: StudySessionEntity): Long = withContext(Dispatchers.IO) {
        database.studySessionDao().insertSession(session)
    }

    suspend fun insertAdmissionTarget(target: AdmissionTargetEntity): Long = withContext(Dispatchers.IO) {
        database.admissionDao().insertTarget(target)
    }

    suspend fun updateAdmissionTarget(target: AdmissionTargetEntity) = withContext(Dispatchers.IO) {
        database.admissionDao().updateTarget(target)
    }

    suspend fun deleteAdmissionTarget(id: Long) = withContext(Dispatchers.IO) {
        database.admissionDao().deleteTargetById(id)
    }

    suspend fun cycleAdmissionStatus(target: AdmissionTargetEntity) = withContext(Dispatchers.IO) {
        val nextStatus = when (target.status) {
            "NOT_APPLIED" -> "APPLIED"
            "APPLIED" -> "ADMIT_DOWNLOADED"
            "ADMIT_DOWNLOADED" -> "COMPLETED"
            "COMPLETED" -> "NOT_APPLIED"
            else -> "APPLIED"
        }
        database.admissionDao().updateTarget(target.copy(status = nextStatus))
    }

    suspend fun resetToSampleData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        populateInitialData(database)
    }
}
