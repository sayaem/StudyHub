package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision_items")
data class RevisionItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectName: String,
    val completedCount: Int = 0,
    val totalCount: Int = 4,
    val isCompleted: Boolean = false,
    val subjectId: Long? = null,
    val chapterId: Long? = null,
    val lastRevisedAt: Long = System.currentTimeMillis()
)
