package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FocusScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusScheduleDao {
    @Query("SELECT * FROM focus_schedules")
    fun getAllSchedules(): Flow<List<FocusScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FocusScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: FocusScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: FocusScheduleEntity)
}
