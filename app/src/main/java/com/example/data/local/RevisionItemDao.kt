package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RevisionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionItemDao {
    @Query("SELECT * FROM revision_items ORDER BY id ASC")
    fun getAllRevisions(): Flow<List<RevisionItemEntity>>

    @Query("SELECT * FROM revision_items WHERE isCompleted = 0 ORDER BY id ASC")
    fun getPendingRevisions(): Flow<List<RevisionItemEntity>>

    @Query("SELECT * FROM revision_items WHERE isCompleted = 1 ORDER BY id ASC")
    fun getCompletedRevisions(): Flow<List<RevisionItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(item: RevisionItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevisions(items: List<RevisionItemEntity>)

    @Update
    suspend fun updateRevision(item: RevisionItemEntity)

    @Delete
    suspend fun deleteRevision(item: RevisionItemEntity)

    @Query("DELETE FROM revision_items WHERE id = :id")
    suspend fun deleteRevisionById(id: Long)

    @Query("UPDATE revision_items SET completedCount = :completed, isCompleted = :isCompleted, lastRevisedAt = :timestamp WHERE id = :id")
    suspend fun updateProgress(id: Long, completed: Int, isCompleted: Boolean, timestamp: Long)

    @Query("UPDATE revision_items SET completedCount = :completed, isCompleted = :isCompleted, lastRevisedAt = :timestamp, nextRevisionDate = :nextDate WHERE id = :id")
    suspend fun updateProgressAndNextDate(id: Long, completed: Int, isCompleted: Boolean, timestamp: Long, nextDate: Long)

    @Query("UPDATE revision_items SET nextRevisionDate = :nextDate WHERE id = :id")
    suspend fun postponeRevision(id: Long, nextDate: Long)
}
