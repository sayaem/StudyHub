package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdmissionTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdmissionDao {
    @Query("SELECT * FROM admission_targets ORDER BY examDate ASC")
    fun getAllTargets(): Flow<List<AdmissionTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: AdmissionTargetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTargets(targets: List<AdmissionTargetEntity>)

    @Update
    suspend fun updateTarget(target: AdmissionTargetEntity)

    @Delete
    suspend fun deleteTarget(target: AdmissionTargetEntity)

    @Query("DELETE FROM admission_targets WHERE id = :id")
    suspend fun deleteTargetById(id: Long)
}
