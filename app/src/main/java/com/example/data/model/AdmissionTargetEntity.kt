package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admission_targets")
data class AdmissionTargetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val universityName: String,
    val unit: String,
    val applicationDeadline: Long,
    val examDate: Long,
    val status: String = "NOT_APPLIED", // "NOT_APPLIED", "APPLIED", "ADMIT_DOWNLOADED", "COMPLETED"
    val websiteUrl: String = "",
    val notes: String = ""
)
