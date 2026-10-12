package com.fpf.sentinellens.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fpf.sentinellens.data.faces.FaceEntity
import com.fpf.sentinellens.data.faces.FacesDao
import com.fpf.sentinellens.data.logs.LogDao
import com.fpf.sentinellens.data.logs.LogEntity
import com.fpf.sentinellens.data.migrations.MIGRATION_1_2

@Database(
    entities = [
        FaceEntity::class,
        LogEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FaceDatabase : RoomDatabase() {
    abstract fun faceDao(): FacesDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: FaceDatabase? = null
        const val DB_NAME = "face_database"

        const val TAG = "FaceDatabase"

        fun close() {
            INSTANCE?.close()
            INSTANCE = null
        }

        fun getDatabase(application: Application): FaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    application,
                    FaceDatabase::class.java,
                    DB_NAME
                ).setJournalMode(JournalMode.TRUNCATE)
                    .addMigrations(
                        MIGRATION_1_2,
                    )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}


