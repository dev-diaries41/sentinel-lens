package com.fpf.sentinellens.data.logs


import com.fpf.sentinellens.data.mappers.toDomain
import com.fpf.sentinellens.data.mappers.toEntity
import com.fpf.sentinellens.surveillance.DetectionLog
import com.fpf.sentinellens.surveillance.NewDetectionLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogRepository(private val dao: LogDao) {
    fun getLogsFlow(): Flow<List<DetectionLog>> = dao.getAllScanData().map { logs -> logs.map{it.toDomain()} }

    suspend fun insert(log: NewDetectionLog): Int {
        return dao.insert(log.toEntity()).toInt()
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun clear(){
        dao.deleteAll()
    }

}