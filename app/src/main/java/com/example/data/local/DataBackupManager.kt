package com.example.data.local

import android.content.Context
import com.example.data.model.BackupData
import com.example.data.repository.StudyHubRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first

class DataBackupManager(
    private val database: StudyHubDatabase,
    private val repository: StudyHubRepository,
    private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
) {

    suspend fun getDatabaseFile(context: Context): java.io.File {
        return context.getDatabasePath("studyhub_database")
    }

    suspend fun backupDatabase(context: Context, destinationFile: java.io.File) {
        val dbFile = getDatabaseFile(context)
        dbFile.copyTo(destinationFile, overwrite = true)
        // Also copy -shm and -wal files if they exist
        val shmFile = java.io.File(dbFile.path + "-shm")
        if (shmFile.exists()) shmFile.copyTo(java.io.File(destinationFile.path + "-shm"), overwrite = true)
        val walFile = java.io.File(dbFile.path + "-wal")
        if (walFile.exists()) walFile.copyTo(java.io.File(destinationFile.path + "-wal"), overwrite = true)
    }

    suspend fun restoreDatabase(context: Context, sourceFile: java.io.File) {
        database.close()
        val dbFile = getDatabaseFile(context)
        sourceFile.copyTo(dbFile, overwrite = true)
        
        val shmFile = java.io.File(dbFile.path + "-shm")
        val walFile = java.io.File(dbFile.path + "-wal")
        
        val sourceShm = java.io.File(sourceFile.path + "-shm")
        val sourceWal = java.io.File(sourceFile.path + "-wal")
        
        if (sourceShm.exists()) sourceShm.copyTo(shmFile, overwrite = true)
        if (sourceWal.exists()) sourceWal.copyTo(walFile, overwrite = true)
    }

}
