package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.StudyHubDatabase
import com.example.data.model.*
import com.example.domain.backup.BackupManager
import com.example.domain.backup.StudyHubBackupPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class StudyHubPersistenceTest {

    private lateinit var database: StudyHubDatabase
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, StudyHubDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `fresh installation has empty database`() = runBlocking {
        val subjects = database.subjectDao().getAllSubjects().first()
        assertTrue("Fresh install must have zero subjects", subjects.isEmpty())
        val chapters = database.chapterDao().getAllChapters().first()
        assertTrue("Fresh install must have zero chapters", chapters.isEmpty())
        val sessions = database.studySessionDao().getAllSessions().first()
        assertTrue("Fresh install must have zero study sessions", sessions.isEmpty())
    }

    @Test
    fun `subject persistence CRUD operations`() = runBlocking {
        val subjectDao = database.subjectDao()
        val subject = SubjectEntity(nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#6366F1", iconName = "atom", orderIndex = 0)
        val id = subjectDao.insertSubject(subject)
        assertTrue("Inserted subject ID must be valid", id > 0)

        val retrieved = subjectDao.getSubjectById(id).first()
        assertNotNull(retrieved)
        assertEquals("Physics", retrieved?.nameEn)

        subjectDao.deleteSubjectById(id)
        val afterDelete = subjectDao.getAllSubjects().first()
        assertTrue("Subject must be deleted", afterDelete.isEmpty())
    }

    @Test
    fun `chapter syllabus progress calculation`() = runBlocking {
        val subjectDao = database.subjectDao()
        val chapterDao = database.chapterDao()

        val subId = subjectDao.insertSubject(SubjectEntity(nameEn = "Math", nameBn = "গণিত", colorHex = "#6366F1", iconName = "atom"))
        chapterDao.insertChapters(listOf(
            ChapterEntity(subjectId = subId, titleEn = "Algebra", titleBn = "বীজগণিত", status = "COMPLETED"),
            ChapterEntity(subjectId = subId, titleEn = "Geometry", titleBn = "জ্যামিতি", status = "IN_PROGRESS"),
            ChapterEntity(subjectId = subId, titleEn = "Trigonometry", titleBn = "ত্রিকোণমিতি", status = "NOT_STARTED")
        ))

        val chapters = chapterDao.getAllChapters().first()
        assertEquals(3, chapters.size)
        val completed = chapters.count { it.status == "COMPLETED" }
        assertEquals(1, completed)
        val percent = (completed.toFloat() / chapters.size) * 100f
        assertEquals(33.333334f, percent, 0.01f)
    }

    @Test
    fun `study session recording and duration aggregation`() = runBlocking {
        val sessionDao = database.studySessionDao()
        val now = System.currentTimeMillis()
        sessionDao.insertSession(StudySessionEntity(subjectId = 1L, durationSeconds = 1800L, timestamp = now))
        sessionDao.insertSession(StudySessionEntity(subjectId = 1L, durationSeconds = 2700L, timestamp = now))

        val sessions = sessionDao.getAllSessions().first()
        assertEquals(2, sessions.size)
        val totalSecs = sessions.sumOf { it.durationSeconds }
        assertEquals(4500L, totalSecs)
    }

    @Test
    fun `backup payload serialization and deserialization`() = runBlocking {
        val payload = StudyHubBackupPayload(
            timestamp = System.currentTimeMillis(),
            subjects = listOf(SubjectEntity(id = 1L, nameEn = "Chemistry", nameBn = "রসায়ন", colorHex = "#10B981", iconName = "flask")),
            chapters = listOf(ChapterEntity(id = 1L, subjectId = 1L, titleEn = "Atom", titleBn = "পরমাণু")),
            sessions = emptyList(),
            admissions = emptyList(),
            plannerTasks = emptyList(),
            revisions = emptyList(),
            userName = "Arafat",
            isDarkTheme = false,
            language = "en",
            isNotificationsEnabled = true
        )

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(StudyHubBackupPayload::class.java)
        val json = adapter.toJson(payload)
        assertNotNull(json)
        assertTrue(json.contains("Chemistry"))

        val parsed = adapter.fromJson(json)
        assertNotNull(parsed)
        assertEquals("Chemistry", parsed?.subjects?.first()?.nameEn)
        assertEquals("Arafat", parsed?.userName)
    }
}
