package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GameEntity::class,
        SaveStateEntity::class,
        ControlProfileEntity::class,
        ErrorLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class RetroDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun saveStateDao(): SaveStateDao
    abstract fun controlProfileDao(): ControlProfileDao
    abstract fun errorLogDao(): ErrorLogDao

    companion object {
        @Volatile
        private var INSTANCE: RetroDatabase? = null

        fun getInstance(context: Context): RetroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RetroDatabase::class.java,
                    "retro_pc_emulator.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
