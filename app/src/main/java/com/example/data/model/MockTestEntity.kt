package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testName: String,
    val examProvider: String,
    val subjectId: Long?,
    val testDate: Long,
    val totalMarks: Double,
    val obtainedMarks: Double,
    val negativeMarks: Double = 0.0,
    val durationMinutes: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)
