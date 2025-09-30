package com.fpf.sentinellens.data.logs


import kotlinx.coroutines.flow.Flow

class DetectionLogRepository(private val dao: DetectionLogsDao) {
    val allScanData: Flow<List<DetectionLogEntity>> = dao.getAllScanData()

    suspend fun insert(log: DetectionLogEntity): Int {
        return dao.insert(log).toInt()
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun clear(){
        dao.deleteAll()
    }

}