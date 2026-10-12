package com.fpf.sentinellens.data.oldlogs

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OldLogsDao {
    @Query("SELECT * FROM detection_logs ORDER BY date DESC")
    fun getAllScanData(): Flow<List<OldLogEntity>>

    @Insert
    suspend fun insert(log: OldLogEntity): Long

    @Query("DELETE FROM detection_logs where id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM detection_logs")
    suspend fun deleteAll()
}
