package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val chapterId: Long? = null,
    val durationSeconds: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionType: String = "POMODORO", // "POMODORO", "STOPWATCH", "MANUAL"
    val notes: String = ""
)
