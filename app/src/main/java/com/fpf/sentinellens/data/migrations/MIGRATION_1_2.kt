package com.fpf.sentinellens.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE faces RENAME TO faces_old")
        db.execSQL("""
            CREATE TABLE faces (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date INTEGER NOT NULL,
                embeddings TEXT NOT NULL,
                type TEXT NOT NULL,
                name TEXT NOT NULL,
                path TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO faces (date, embeddings, type, name, path)
            SELECT date, embeddings, type, name, id
            FROM faces_old
        """.trimIndent())

        db.execSQL("DROP TABLE faces_old")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS detection_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date INTEGER NOT NULL,
                type TEXT NOT NULL,
                name TEXT,
                similarity REAL NOT NULL
            )
        """.trimIndent())
    }
}