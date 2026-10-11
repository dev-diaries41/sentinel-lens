package com.fpf.sentinellens.data.faces

import kotlinx.coroutines.flow.Flow

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

    suspend fun isExist(id: String): Boolean {
        return dao.isExist(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }


}
