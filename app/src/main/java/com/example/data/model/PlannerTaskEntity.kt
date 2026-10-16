package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planner_tasks")
data class PlannerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timeSlot: String, // e.g. "08:00 - 09:30"
    val subjectTitle: String,
    val topicTitle: String,
    val dateCategory: String = "TODAY", // "TODAY", "TOMORROW", "THIS_WEEK"
    val isCompleted: Boolean = false,
    val colorHex: String = "#2563EB",
    val subjectId: Long? = null,
    val chapterId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
