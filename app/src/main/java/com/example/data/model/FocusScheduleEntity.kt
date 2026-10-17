package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_schedules")
data class FocusScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val daysOfWeek: List<Int>, // 0-6 (Sun-Sat)
    val startTime: Int, // Minutes from midnight
    val endTime: Int,
    val restrictedApps: List<String>,
    val isEnabled: Boolean = true
)
