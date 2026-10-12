package com.fpf.sentinellens.data.oldlogs

import android.app.Application
import androidx.room.*

@Database(entities = [OldLogEntity::class], version = 1, exportSchema = false)
abstract class DetectionLogDatabase : RoomDatabase() {
    abstract fun detectionLogsDao(): OldLogsDao

    companion object {
        @Volatile
        private var INSTANCE: DetectionLogDatabase? = null

        fun getDatabase(application: Application): DetectionLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    DetectionLogDatabase::class.java,
                    "detection_log_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


