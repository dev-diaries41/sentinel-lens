package com.fpf.sentinellens.data.logs

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM detection_logs ORDER BY date DESC")
    fun getAllScanData(): Flow<List<LogEntity>>

    @Insert
    suspend fun insert(log: LogEntity): Long

    @Query("DELETE FROM detection_logs where id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM detection_logs")
    suspend fun deleteAll()
}
