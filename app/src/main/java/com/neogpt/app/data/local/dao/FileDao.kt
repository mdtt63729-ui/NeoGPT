package com.neogpt.app.data.local.dao

import androidx.room.*
import com.neogpt.app.data.local.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY createdAt DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)
}
