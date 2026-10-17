package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mistakes")
data class MistakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long?,
    val chapterId: Long?,
    val questionDescription: String,
    val mistakeDescription: String,
    val correctApproach: String,
    val mistakeCategory: String, // "Conceptual", "Calculation", "Careless", "Memory", "TimeManagement"
    val dateRecorded: Long = System.currentTimeMillis(),
    val status: String = "NEW" // "NEW", "REVIEWED", "RESOLVED"
)
