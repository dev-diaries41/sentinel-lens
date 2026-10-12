package com.fpf.sentinellens.data

import android.content.Context
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.logs.LogRepository
import com.fpf.sentinellens.data.oldlogs.OldLogRepository
import com.fpf.sentinellens.surveillance.DetectionTypes
import com.fpf.sentinellens.surveillance.NewDetectionLog
import kotlin.io.relativeTo

object DataSyncHelper {

    private const val TAG = "DataSyncHelper"
    suspend fun transferLogs(oldLogRepository: OldLogRepository, logRepository: LogRepository){
        val oldLogs = oldLogRepository.getAll()
        logRepository.insert(oldLogs.map{ NewDetectionLog (
            date = it.date,
            type = DetectionTypes.entries.find { entry-> entry.value == it.type }?.key!!,
            name=it.name,
            similarity = it.similarity
        )})
        oldLogRepository.clear()
        Log.d(TAG, "Transferred ${oldLogs.size} logs")

    }
    suspend fun transformFacePaths(context: Context, facesRepository: FacesRepository){
        val faces = facesRepository.getAllFacesSync()
        val updatedFaces = faces.map { it.copy(path = transformPath(context, it.path)) }
        facesRepository.update(updatedFaces)
        Log.d(TAG, "Updated ${faces.size} face paths")
    }

    private fun transformPath(context: Context, path: String): String = path.toUri().toFile().relativeTo(context.filesDir).path
}