package com.fpf.sentinellens.data.oldlogs


class OldLogRepository(private val dao: OldLogsDao) {
    suspend fun getAll(): List<OldLogEntity> = dao.getAllLogs()

    suspend fun insert(log: OldLogEntity): Long = dao.insert(log)

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun clear()= dao.deleteAll()
}