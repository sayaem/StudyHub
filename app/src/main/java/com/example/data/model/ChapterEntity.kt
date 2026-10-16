package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val titleEn: String,
    val titleBn: String,
    val status: String = "NOT_STARTED", // "NOT_STARTED", "IN_PROGRESS", "COMPLETED"
    val priority: String = "MEDIUM",     // "HIGH", "MEDIUM", "LOW"
    val isRevisionNeeded: Boolean = false,
    val revisionCount: Int = 0,
    val lastRevisedAt: Long? = null,
    val estimatedMinutes: Int = 60,
    val notes: String = ""
)
