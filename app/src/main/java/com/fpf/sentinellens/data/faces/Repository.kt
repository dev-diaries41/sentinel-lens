package com.fpf.sentinellens.data.faces

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import java.io.File

class FacesRepository(private val dao: FacesDao) {
    val allFaces: Flow<List<Face>> = dao.getAllFaces()
    val hasAnyFaces: Flow<Boolean> = dao.hasAnyFaces()

    suspend fun getAllFacesSync(): List<Face> {
        return dao.getAllFacesSync()
    }

    suspend fun getFace(id: String): Face{
        return dao.getFace(id)
    }

    suspend fun insert(face: Face) {
        dao.insertFace(face)
    }

    suspend fun delete(id: String): Int {
        return dao.deleteFace(id)
    }

    suspend fun updateFaceId(oldId: String, newId: String) {
        dao.updateFaceId(oldId, newId)
    }

    suspend fun isExist(id: String): Boolean {
        return dao.isExist(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun migratePickerUrisToLocal(context: Context, onComplete: () -> Unit) {
        val faces = dao.getAllFacesSync()

        faces.forEach { face ->
            if (face.id.startsWith("content://")) {
                val filePath = "faces/${face.id.hashCode()}.jpg"
                val file = File(context.filesDir, filePath)
                if(!file.exists()) return
                val newId = file.toUri().toString()
                updateFaceId(face.id, newId)
            }
        }
        onComplete()
    }

}
