package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AdmissionTargetEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        StudySessionEntity::class,
        AdmissionTargetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyHubDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun chapterDao(): ChapterDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun admissionDao(): AdmissionDao

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
                    .addCallback(StudyHubDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class StudyHubDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: StudyHubDatabase) {
    val subjectDao = database.subjectDao()
    val chapterDao = database.chapterDao()
    val admissionDao = database.admissionDao()
    val sessionDao = database.studySessionDao()

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

    // Physics: 8 / 12 chapters completed (67%)
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = physicsId, titleEn = "Mechanics", titleBn = "বলবিদ্যা", status = "COMPLETED", priority = "HIGH", revisionCount = 2, estimatedMinutes = 90),
        ChapterEntity(subjectId = physicsId, titleEn = "Thermodynamics", titleBn = "তাপগতিবিদ্যা", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 1, estimatedMinutes = 110),
        ChapterEntity(subjectId = physicsId, titleEn = "Electrostatics", titleBn = "স্থির তড়িৎ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 2, estimatedMinutes = 90, notes = "4 topics left"),
        ChapterEntity(subjectId = physicsId, titleEn = "Current Electricity", titleBn = "চল তড়িৎ", status = "NOT_STARTED", priority = "MEDIUM", estimatedMinutes = 90),
        ChapterEntity(subjectId = physicsId, titleEn = "Modern Physics", titleBn = "আধুনিক পদার্থবিজ্ঞান", status = "NOT_STARTED", priority = "MEDIUM", isRevisionNeeded = true, estimatedMinutes = 80),
        ChapterEntity(subjectId = physicsId, titleEn = "Optics", titleBn = "আলোকবিজ্ঞান", status = "NOT_STARTED", priority = "LOW", estimatedMinutes = 75),
        // Additional completed to make 8 / 12
        ChapterEntity(subjectId = physicsId, titleEn = "Vectors", titleBn = "ভেক্টর", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = physicsId, titleEn = "Work, Energy & Power", titleBn = "কাজ, শক্তি ও ক্ষমতা", status = "COMPLETED", priority = "HIGH", revisionCount = 1),
        ChapterEntity(subjectId = physicsId, titleEn = "Gravitation & Gravity", titleBn = "মহাকর্ষ ও অভিকর্ষ", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = physicsId, titleEn = "Structural Properties", titleBn = "পদার্থের গাঠনিক ধর্ম", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1),
        ChapterEntity(subjectId = physicsId, titleEn = "Periodic Motion", titleBn = "পর্যায়বৃত্ত গতি", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = physicsId, titleEn = "Waves & Sound", titleBn = "তরঙ্গ ও শব্দ", status = "COMPLETED", priority = "MEDIUM", revisionCount = 1)
    ))

    // Chemistry: 5 / 10 chapters completed (50%)
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

    // Mathematics: 6 / 12 chapters completed (50%)
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

    // English: 2 / 6 chapters completed (33%)
    chapterDao.insertChapters(listOf(
        ChapterEntity(subjectId = englishId, titleEn = "Parts of Speech & Verbs", titleBn = "পদ প্রকরণ ও ক্রিয়া", status = "COMPLETED", priority = "HIGH", revisionCount = 2),
        ChapterEntity(subjectId = englishId, titleEn = "Prepositions & Idioms", titleBn = "প্রিপজিশন ও বাগধারা", status = "COMPLETED", priority = "HIGH", revisionCount = 1),
        ChapterEntity(subjectId = englishId, titleEn = "English Grammar & Clauses", titleBn = "ইংরেজি ব্যাকরণ ও ক্লজ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 0),
        ChapterEntity(subjectId = englishId, titleEn = "Vocabulary & Synonyms", titleBn = "শব্দভাণ্ডার ও সমার্থক শব্দ", status = "IN_PROGRESS", priority = "HIGH", isRevisionNeeded = true, revisionCount = 1),
        ChapterEntity(subjectId = englishId, titleEn = "Reading Comprehension", titleBn = "বোধগম্যতা পরীক্ষা", status = "NOT_STARTED", priority = "MEDIUM"),
        ChapterEntity(subjectId = englishId, titleEn = "Sentence Correction", titleBn = "বাক্য শুদ্ধিকরণ", status = "NOT_STARTED", priority = "MEDIUM")
    ))

    // Biology: 0 / 8 chapters completed (0%)
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

    // University Admissions matching exact mockup:
    // 1. DU Admission Test: University of Dhaka, 12 May 2025, 26 days left (or 18 days for hero card)
    // 2. BUET Admission: Bangladesh University of Engineering..., 25 May 2025, 39 days left
    // 3. GST Admission: General, Science and Technology, 10 Jun 2025, 55 days left
    // 4. Medical Admission: MBBS (Public & Private), 15 Jul 2025, 91 days left
    val now = System.currentTimeMillis()
    val dayMs = 86_400_000L
    admissionDao.insertTargets(listOf(
        AdmissionTargetEntity(
            universityName = "DU Admission Test",
            unit = "University of Dhaka",
            applicationDeadline = now + (10 * dayMs),
            examDate = now + (18 * dayMs), // Exactly 18 days left as shown in the hero badge!
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

    // Today's Study Sessions: 2 hours 15 minutes = 8100 seconds
    val midnightToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    sessionDao.insertSession(
        StudySessionEntity(
            subjectId = physicsId,
            durationSeconds = 4800, // 1h 20m
            timestamp = midnightToday + (10 * 3600 * 1000L),
            sessionType = "POMODORO",
            notes = "Electrostatics point charge and potential"
        )
    )
    sessionDao.insertSession(
        StudySessionEntity(
            subjectId = chemistryId,
            durationSeconds = 3300, // 55m -> total 8100s = exactly 2h 15m!
            timestamp = midnightToday + (13 * 3600 * 1000L),
            sessionType = "POMODORO",
            notes = "Organic chemistry mechanisms"
        )
    )

    // Past 6 days sessions for weekly analytics (Mon to Sun) totaling 18h 45m
    val day1 = midnightToday - (1 * dayMs)
    val day2 = midnightToday - (2 * dayMs)
    val day3 = midnightToday - (3 * dayMs)
    val day4 = midnightToday - (4 * dayMs)
    val day5 = midnightToday - (5 * dayMs)
    val day6 = midnightToday - (6 * dayMs)

    sessionDao.insertSession(StudySessionEntity(subjectId = mathId, durationSeconds = 10800, timestamp = day1, sessionType = "STOPWATCH", notes = "Calculus problem sets")) // 3h
    sessionDao.insertSession(StudySessionEntity(subjectId = physicsId, durationSeconds = 9000, timestamp = day2, sessionType = "POMODORO", notes = "Thermodynamics problem bank")) // 2.5h
    sessionDao.insertSession(StudySessionEntity(subjectId = chemistryId, durationSeconds = 12600, timestamp = day3, sessionType = "POMODORO", notes = "Equilibrium & Qualitative")) // 3.5h
    sessionDao.insertSession(StudySessionEntity(subjectId = englishId, durationSeconds = 7200, timestamp = day4, sessionType = "POMODORO", notes = "Grammar rules")) // 2h
    sessionDao.insertSession(StudySessionEntity(subjectId = mathId, durationSeconds = 10800, timestamp = day5, sessionType = "STOPWATCH", notes = "Coordinate geometry")) // 3h
    sessionDao.insertSession(StudySessionEntity(subjectId = physicsId, durationSeconds = 9000, timestamp = day6, sessionType = "POMODORO", notes = "Mechanics review")) // 2.5h
}
