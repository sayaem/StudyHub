package com.example.domain.focus

import com.example.data.model.FocusScheduleEntity
import java.util.Calendar

class FocusEngine {

    fun isAppRestricted(schedules: List<FocusScheduleEntity>, packageName: String): Boolean {
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1 // 0-6
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        return schedules.any { schedule ->
            schedule.isEnabled &&
                    schedule.daysOfWeek.contains(currentDay) &&
                    currentMinutes in schedule.startTime..schedule.endTime &&
                    schedule.restrictedApps.contains(packageName)
        }
    }
}
