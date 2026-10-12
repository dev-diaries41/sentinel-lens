package com.fpf.sentinellens.data.oldlogs


import kotlinx.coroutines.flow.Flow

class OldLogRepository(private val dao: OldLogsDao) {
    val allScanData: Flow<List<OldLogEntity>> = dao.getAllScanData()

    suspend fun insert(log: OldLogEntity): Int {
        return dao.insert(log).toInt()
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun clear(){
        dao.deleteAll()
    }

}