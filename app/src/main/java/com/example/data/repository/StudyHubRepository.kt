package com.example.data.repository

import com.example.data.local.StudyHubDatabase
import com.example.data.local.populateSampleDemoData
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.MockTestEntity
import com.example.data.model.MistakeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudyHubRepository(private val database: StudyHubDatabase) {

    val allSubjects: Flow<List<SubjectEntity>> = database.subjectDao().getAllSubjects()
    val allChapters: Flow<List<ChapterEntity>> = database.chapterDao().getAllChapters()
    val revisionChapters: Flow<List<ChapterEntity>> = database.chapterDao().getRevisionChapters()
    val allSessions: Flow<List<StudySessionEntity>> = database.studySessionDao().getAllSessions()
    val allAdmissions: Flow<List<AdmissionTargetEntity>> = database.admissionDao().getAllTargets()
    val allPlannerTasks: Flow<List<PlannerTaskEntity>> = database.plannerTaskDao().getAllTasks()
    val allRevisions: Flow<List<RevisionItemEntity>> = database.revisionItemDao().getAllRevisions()
    val allMockTests: Flow<List<MockTestEntity>> = database.mockTestDao().getAllMockTests()
    val allMistakes: Flow<List<MistakeEntity>> = database.mistakeDao().getAllMistakes()

    fun getTasksByCategory(category: String): Flow<List<PlannerTaskEntity>> =
        database.plannerTaskDao().getTasksByCategory(category)

    fun getChaptersForSubject(subjectId: Long): Flow<List<ChapterEntity>> =
        database.chapterDao().getChaptersBySubject(subjectId)

    suspend fun insertSubject(subject: SubjectEntity): Long = withContext(Dispatchers.IO) {
        database.subjectDao().insertSubject(subject)
    }

    suspend fun updateSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        database.subjectDao().updateSubject(subject)
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

    suspend fun updateSession(session: StudySessionEntity) = withContext(Dispatchers.IO) {
        database.studySessionDao().updateSession(session)
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

    // Planner Tasks
    suspend fun insertPlannerTask(task: PlannerTaskEntity): Long = withContext(Dispatchers.IO) {
        database.plannerTaskDao().insertTask(task)
    }

    suspend fun insertPlannerTasks(tasks: List<PlannerTaskEntity>) = withContext(Dispatchers.IO) {
        database.plannerTaskDao().insertTasks(tasks)
    }

    suspend fun updatePlannerTaskCompletion(id: Long, completed: Boolean) = withContext(Dispatchers.IO) {
        database.plannerTaskDao().updateTaskCompletion(id, completed)
    }

    suspend fun deletePlannerTask(id: Long) = withContext(Dispatchers.IO) {
        database.plannerTaskDao().deleteTaskById(id)
    }

    // Revision Items
    suspend fun insertRevision(item: RevisionItemEntity): Long = withContext(Dispatchers.IO) {
        database.revisionItemDao().insertRevision(item)
    }

    suspend fun updateRevisionProgress(id: Long, completedCount: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        database.revisionItemDao().updateProgress(
            id = id,
            completed = completedCount,
            isCompleted = isCompleted,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun deleteRevision(id: Long) = withContext(Dispatchers.IO) {
        database.revisionItemDao().deleteRevisionById(id)
    }

    // Mock Tests
    suspend fun insertMockTest(test: MockTestEntity): Long = withContext(Dispatchers.IO) {
        database.mockTestDao().insertMockTest(test)
    }

    suspend fun deleteMockTest(test: MockTestEntity) = withContext(Dispatchers.IO) {
        database.mockTestDao().deleteMockTest(test)
    }

    // Mistakes
    suspend fun insertMistake(mistake: MistakeEntity): Long = withContext(Dispatchers.IO) {
        database.mistakeDao().insertMistake(mistake)
    }

    suspend fun updateMistake(mistake: MistakeEntity) = withContext(Dispatchers.IO) {
        database.mistakeDao().updateMistake(mistake)
    }

    suspend fun deleteMistake(mistake: MistakeEntity) = withContext(Dispatchers.IO) {
        database.mistakeDao().deleteMistake(mistake)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    suspend fun loadDemoData() = withContext(Dispatchers.IO) {
        populateSampleDemoData(database)
    }
}
