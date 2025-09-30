package com.fpf.sentinellens.data.faces

import androidx.lifecycle.LiveData

class FacesRepository(private val dao: FacesDao) {
    val allFaces: LiveData<List<Face>> = dao.getAllFaces()
    val hasAnyFaces: LiveData<Boolean> = dao.hasAnyFaces()

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

    suspend fun isExist(id: String): Boolean {
        return dao.isExist(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

}
