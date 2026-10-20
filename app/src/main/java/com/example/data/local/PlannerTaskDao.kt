package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlannerTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerTaskDao {
    @Query("SELECT * FROM planner_tasks ORDER BY id ASC")
    fun getAllTasks(): Flow<List<PlannerTaskEntity>>

    @Query("SELECT * FROM planner_tasks WHERE dateCategory = :category ORDER BY id ASC")
    fun getTasksByCategory(category: String): Flow<List<PlannerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<PlannerTaskEntity>)

    @Update
    suspend fun updateTask(task: PlannerTaskEntity)

    @Delete
    suspend fun deleteTask(task: PlannerTaskEntity)

    @Query("DELETE FROM planner_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long): Int

    @Query("UPDATE planner_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, completed: Boolean): Int
}
