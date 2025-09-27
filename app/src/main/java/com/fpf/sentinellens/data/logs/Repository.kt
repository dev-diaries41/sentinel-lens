package com.fpf.sentinellens.data.logs


import kotlinx.coroutines.flow.Flow

class DetectionLogRepository(private val dao: DetectionLogsDao) {
    val allScanData: Flow<List<DetectionLogEntity>> = dao.getAllScanData()

    suspend fun insert(scanData: DetectionLogEntity): Int {
        return dao.insert(scanData).toInt()
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

}