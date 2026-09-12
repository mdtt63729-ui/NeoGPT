package com.neogpt.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neogpt.app.data.local.dao.*
import com.neogpt.app.data.local.entity.*

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        ProjectEntity::class,
        FileEntity::class,
        TaskEntity::class,
        MemoryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class NeoDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun projectDao(): ProjectDao
    abstract fun fileDao(): FileDao
    abstract fun taskDao(): TaskDao
    abstract fun memoryDao(): MemoryDao
}
