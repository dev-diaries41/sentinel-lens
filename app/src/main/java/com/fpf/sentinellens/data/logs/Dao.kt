package com.fpf.sentinellens.data.logs

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionLogsDao {
    @Query("SELECT * FROM detection_logs ORDER BY date DESC")
    fun getAllScanData(): Flow<List<DetectionLogEntity>>

    @Insert
    suspend fun insert(scanData: DetectionLogEntity): Long

    @Query("DELETE FROM detection_logs where id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM detection_logs")
    suspend fun deleteAll()
}
