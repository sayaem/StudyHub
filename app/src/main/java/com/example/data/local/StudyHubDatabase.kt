package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.PlannerTaskEntity
import com.example.data.model.RevisionItemEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.CoroutineScope
import java.util.Calendar

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        StudySessionEntity::class,
        AdmissionTargetEntity::class,
        PlannerTaskEntity::class,
        RevisionItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudyHubDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun admissionDao(): AdmissionDao
    abstract fun plannerTaskDao(): PlannerTaskDao
    abstract fun revisionItemDao(): RevisionItemDao

    companion object {
        @Volatile
        private var INSTANCE: StudyHubDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): StudyHubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyHubDatabase::class.java,
                    "studyhub_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// Explicit demo loader for optional demo mode testing
suspend fun populateSampleDemoData(database: StudyHubDatabase) {
    val subjectDao = database.subjectDao()
    val chapterDao = database.chapterDao()
    val admissionDao = database.admissionDao()
    val sessionDao = database.studySessionDao()
    val plannerDao = database.plannerTaskDao()
    val revisionDao = database.revisionItemDao()

    database.clearAllTables()

    val physicsId = subjectDao.insertSubject(
        SubjectEntity(nameEn = "Physics", nameBn = "পদার্থবিজ্ঞান", colorHex = "#6366F1", iconName = "atom", orderIndex = 0)
    )
    val chemistryId = subjectDao.insertSubject(
        SubjectEntity(nameEn = "Chemistry", nameBn = "রসায়ন", colorHex = "#10B981", iconName = "flask", orderIndex = 1)
    )
    val mathId = subjectDao.insertSubject(
        SubjectEntity(nameEn = "Mathematics", nameBn = "উচ্চতর গণিত", colorHex = "#8B5CF6", iconName = "calculator", orderIndex = 2)
    )
    val englishId = subjectDao.insertSubject(
        SubjectEntity(nameEn = "English", nameBn = "ইংরেজি", colorHex = "#EF4444", iconName = "book", orderIndex = 3)
    )
    val biologyId = subjectDao.insertSubject(
        SubjectEntity(nameEn = "Biology", nameBn = "জীববিজ্ঞান", colorHex = "#10B981", iconName = "leaf", orderIndex = 4)
    )

    // Physics
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = physicsId, titleEn = "Mechanics", titleBn = "বলবিদ্যা", status = "COMPLETED", priority = "HIGH", revisionCount = 2, estimatedMinutes = 90),
        ChapterEntity(subjectId = physicsId, titleEn = "Thermodynamics", titleBn = "তাপগতিবিদ্যা", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 1, estimatedMinutes = 110),
        ChapterEntity(subjectId = physicsId, titleEn = "Electrostatics", titleBn = "স্থির তড়িৎ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 2, estimatedMinutes = 90, notes = "4 topics left"),
        ChapterEntity(subjectId = physicsId, titleEn = "Current Electricity", titleBn = "চল তড়িৎ", status = "NOT_STARTED", priority = "MEDIUM", estimatedMinutes = 90),
        ChapterEntity(subjectId = physicsId, titleEn = "Modern Physics", titleBn = "আধুনিক পদার্থবিজ্ঞান", status = "NOT_STARTED", priority = "MEDIUM", isRevisionNeeded = true, estimatedMinutes = 80),
        ChapterEntity(subjectId = physicsId, titleEn = "Optics", titleBn = "আলোকবিজ্ঞান", status = "NOT_STARTED", priority = "LOW", estimatedMinutes = 75),
        ChapterEntity(subjectId = physicsId, titleEn = "Vectors", titleBn = "ভেক্টর", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = physicsId, titleEn = "Work, Energy & Power", titleBn = "কাজ, শক্তি ও ক্ষমতা", status = "COMPLETED", priority = "HIGH", revisionCount = 1),
        ChapterEntity(subjectId = physicsId, titleEn = "Gravitation & Gravity", titleBn = "মহাকর্ষ ও অভিকর্ষ", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = physicsId, titleEn = "Structural Properties", titleBn = "পদার্থের গাঠনিক ধর্ম", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = physicsId, titleEn = "Periodic Motion", titleBn = "পর্যায়বৃত্ত গতি", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = physicsId, titleEn = "Waves & Sound", titleBn = "তরঙ্গ ও শব্দ", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1)
    ))

    // Chemistry
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = chemistryId, titleEn = "Qualitative Chemistry", titleBn = "গুণগত রসায়ন", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = chemistryId, titleEn = "Periodic Table & Bonding", titleBn = "পর্যায়বৃত্ত ধর্ম ও বন্ধন", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = chemistryId, titleEn = "Chemical Equilibrium", titleBn = "রাসায়নিক পরিবর্তন ও সাম্যাবস্থা", status = "COMPLETED", priority = "HIGH", revisionCount = 1),
        ChapterEntity(subjectId = chemistryId, titleEn = "Gas Laws & Environment", titleBn = "পরিবেশ রসায়ন", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = chemistryId, titleEn = "Quantitative Chemistry", titleBn = "পরিমাণগত রসায়ন", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = chemistryId, titleEn = "Organic Chemistry", titleBn = "জৈব রসায়ন", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 0),
        ChapterEntity(subjectId = chemistryId, titleEn = "Electrochemistry", titleBn = "তড়িৎ রসায়ন", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = chemistryId, titleEn = "Industrial Chemistry", titleBn = "অর্থনৈতিক রসায়ন", status = "NOT_STARTED", priority = "LOW"),
        ChapterEntity(subjectId = chemistryId, titleEn = "Thermochemistry", titleBn = "তাপ রসায়ন", status = "NOT_STARTED", priority = "LOW"),
        ChapterEntity(subjectId = chemistryId, titleEn = "Acid Base & Buffer", titleBn = "অম্ল-ক্ষারক সাম্যাবস্থা", status = "NOT_STARTED", priority = "MEDIUM")
    ))

    // Mathematics
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = mathId, titleEn = "Matrices & Determinants", titleBn = "ম্যাট্রিক্স ও নির্ণায়ক", status = "COMPLETED", priority = "LOW", revisionCount = 1),
        ChapterEntity(subjectId = mathId, titleEn = "Straight Lines", titleBn = "সরলরেখা", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = mathId, titleEn = "Circles", titleBn = "বৃত্ত", status = "COMPLETED", priority = "HIGH", revisionCount = 1),
        ChapterEntity(subjectId = mathId, titleEn = "Trigonometry", titleBn = "ত্রিকোণমিতি", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = mathId, titleEn = "Permutation & Combination", titleBn = "বিন্যাস ও সমাবেশ", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = mathId, titleEn = "Binomial Theorem", titleBn = "দ্বিপদী বিস্তৃতি", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = mathId, titleEn = "Calculus (Differentiation)", titleBn = "অন্তরীকরণ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 1),
        ChapterEntity(subjectId = mathId, titleEn = "Calculus (Integration)", titleBn = "যোগজীকরণ", status = "NOT_STARTED", priority = "HIGH"),
        ChapterEntity(subjectId = mathId, titleEn = "Conics", titleBn = "কণিক", status = "NOT_STARTED", priority = "HIGH"),
        ChapterEntity(subjectId = mathId, titleEn = "Complex Numbers", titleBn = "জটিল সংখ্যা", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = mathId, titleEn = "Polynomials", titleBn = "বহুপদী ও বহুপদী সমীকরণ", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = mathId, titleEn = "Statics & Dynamics", titleBn = "স্থিতিবিদ্যা ও গতিবিদ্যা", status = "NOT_STARTED", priority = "HIGH")
    ))

    // English
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = englishId, titleEn = "Parts of Speech & Verbs", titleBn = "পদ প্রকরণ ও ক্রিয়া", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = englishId, titleEn = "Prepositions & Idioms", titleBn = "প্রিপজিশন ও বাগধারা", status = "COMPLETED", priority = "HIGH", revisionCount = 1),
        ChapterEntity(subjectId = englishId, titleEn = "English Grammar & Clauses", titleBn = "ইংরেজি ব্যাকরণ ও ক্লজ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 0),
        ChapterEntity(subjectId = englishId, titleEn = "Vocabulary & Synonyms", titleBn = "শব্দভাণ্ডার ও সমার্থক শব্দ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 1),
        ChapterEntity(subjectId = englishId, titleEn = "Reading Comprehension", titleBn = "বোধগম্যতা পরীক্ষা", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = englishId, titleEn = "Sentence Correction", titleBn = "বাক্য শুদ্ধিকরণ", status = "NOT_STARTED", priority = "MEDIUM")
    ))

    // Biology
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = biologyId, titleEn = "Cell & Structure", titleBn = "কোষ ও এর গঠন", status = "NOT_STARTED", priority = "HIGH"),
        ChapterEntity(subjectId = biologyId, titleEn = "Cell Division", titleBn = "কোষ বিভাজন", status = "NOT_STARTED", priority = "HIGH"),
        ChapterEntity(subjectId = biologyId, titleEn = "Plant Physiology", titleBn = "উদ্ভিদ শারীরতত্ত্ব", status = "NOT_STARTED", priority = "HIGH"),
        ChapterEntity(subjectId = biologyId, titleEn = "Genetics", titleBn = "জিনতত্ত্ব", status = "NOT_STARTED", priority = "HIGH"),
        ChapterEntity(subjectId = biologyId, titleEn = "Circulation & Blood", titleBn = "রক্ত ও সংবহন", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = biologyId, titleEn = "Digestive System", titleBn = "পরিপাক ও শোষণ", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = biologyId, titleEn = "Respiratory System", titleBn = "শ্বসন ও শ্বাসক্রিয়া", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = biologyId, titleEn = "Human Immunity", titleBn = "মানব প্রতিরক্ষা ও ইমিউনিটি", status = "NOT_STARTED", priority = "HIGH")
    ))

    // Admission targets
    val now = System.currentTimeMillis()
    val dayMs = 86_400_000L
    admissionDao.insertTargets(listOf(
        AdmissionTargetEntity(
            universityName = "DU Admission Test",
            unit = "University of Dhaka",
            applicationDeadline = now + (10 * dayMs),
            examDate = now + (18 * dayMs),
            status = "APPLIED",
            websiteUrl = "admission.eis.du.ac.bd",
            notes = "Centres: Curzon Hall, Arts Building. Unit A."
        ),
        AdmissionTargetEntity(
            universityName = "BUET Admission",
            unit = "Bangladesh University of Engineering...",
            applicationDeadline = now + (20 * dayMs),
            examDate = now + (39 * dayMs),
            status = "NOT_APPLIED",
            websiteUrl = "buet.ac.bd",
            notes = "Preliminary MCQ screening followed by Written Exam."
        ),
        AdmissionTargetEntity(
            universityName = "GST Admission",
            unit = "General, Science and Technology",
            applicationDeadline = now + (30 * dayMs),
            examDate = now + (55 * dayMs),
            status = "NOT_APPLIED",
            websiteUrl = "gstadmission.ac.bd",
            notes = "Combined admission test for 24 public universities."
        ),
        AdmissionTargetEntity(
            universityName = "Medical Admission",
            unit = "MBBS (Public & Private)",
            applicationDeadline = now + (45 * dayMs),
            examDate = now + (91 * dayMs),
            status = "APPLIED",
            websiteUrl = "dgme.teletalk.com.bd",
            notes = "National admission test by DGHS."
        )
    ))

    // Planner tasks
    plannerDao.insertTasks(listOf(
        PlannerTaskEntity(timeSlot = "08:00 - 09:30", subjectTitle = "Physics", topicTitle = "Electrostatics", dateCategory = "TODAY", isCompleted = true, colorHex = "#10B981"),
        PlannerTaskEntity(timeSlot = "10:00 - 11:00", subjectTitle = "Chemistry", topicTitle = "Organic", dateCategory = "TODAY", isCompleted = false, colorHex = "#2563EB"),
        PlannerTaskEntity(timeSlot = "01:00 - 02:00", subjectTitle = "English", topicTitle = "Vocabulary", dateCategory = "TODAY", isCompleted = false, colorHex = "#2563EB"),
        PlannerTaskEntity(timeSlot = "04:00 - 05:30", subjectTitle = "Mathematics", topicTitle = "Calculus", dateCategory = "TODAY", isCompleted = false, colorHex = "#2563EB")
    ))

    // Revision items
    revisionDao.insertRevisions(listOf(
        RevisionItemEntity(title = "Electrostatics", subjectName = "Physics", completedCount = 2, totalCount = 4, isCompleted = false),
        RevisionItemEntity(title = "Organic Chemistry", subjectName = "Chemistry", completedCount = 0, totalCount = 3, isCompleted = false),
        RevisionItemEntity(title = "Calculus", subjectName = "Mathematics", completedCount = 1, totalCount = 5, isCompleted = false),
        RevisionItemEntity(title = "English Grammar", subjectName = "English", completedCount = 0, totalCount = 2, isCompleted = false),
        RevisionItemEntity(title = "Modern Physics", subjectName = "Physics", completedCount = 0, totalCount = 4, isCompleted = false),
        RevisionItemEntity(title = "Mechanics", subjectName = "Physics", completedCount = 4, totalCount = 4, isCompleted = true)
    ))

    // Sessions: 2h 15m today
    val midnightToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    sessionDao.insertSession(
        StudySessionEntity(
            subjectId = physicsId,
            durationSeconds = 4800,
            timestamp = midnightToday + (10 * 3600 * 1000L),
            sessionType = "POMODORO",
            notes = "Electrostatics problem sets"
        )
    )
    sessionDao.insertSession(
        StudySessionEntity(
            subjectId = chemistryId,
            durationSeconds = 3300,
            timestamp = midnightToday + (13 * 3600 * 1000L),
            sessionType = "POMODORO",
            notes = "Organic chemistry mechanisms"
        )
    )

    // Past 6 days
    sessionDao.insertSession(StudySessionEntity(subjectId = mathId, durationSeconds = 10800, timestamp = midnightToday - (1 * dayMs), sessionType = "STOPWATCH"))
    sessionDao.insertSession(StudySessionEntity(subjectId = physicsId, durationSeconds = 9000, timestamp = midnightToday - (2 * dayMs), sessionType = "POMODORO"))
    sessionDao.insertSession(StudySessionEntity(subjectId = chemistryId, durationSeconds = 12600, timestamp = midnightToday - (3 * dayMs), sessionType = "POMODORO"))
    sessionDao.insertSession(StudySessionEntity(subjectId = englishId, durationSeconds = 7200, timestamp = midnightToday - (4 * dayMs), sessionType = "POMODORO"))
    sessionDao.insertSession(StudySessionEntity(subjectId = mathId, durationSeconds = 10800, timestamp = midnightToday - (5 * dayMs), sessionType = "STOPWATCH"))
    sessionDao.insertSession(StudySessionEntity(subjectId = physicsId, durationSeconds = 9000, timestamp = midnightToday - (6 * dayMs), sessionType = "POMODORO"))
}
