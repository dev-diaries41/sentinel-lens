package com.fpf.sentinellens.data.faces

import com.fpf.sentinellens.data.mappers.toDomain
import com.fpf.sentinellens.data.mappers.toEntity
import com.fpf.sentinellens.surveillance.Face
import com.fpf.sentinellens.surveillance.NewFace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FacesRepository(private val dao: FacesDao) {
    val allFaces: Flow<List<Face>> = dao.getAllFaces().map { faces -> faces.map{it.toDomain()} }
    val hasAnyFaces: Flow<Boolean> = dao.hasAnyFaces()

    suspend fun getAllFacesSync(): List<Face> {
        return dao.getAllFacesSync().map{it.toDomain()}
    }

    suspend fun getFace(id: Long): Face{
        return dao.getById(id).toDomain()
    }

    suspend fun insert(face: NewFace)  = dao.insert(face.toEntity())

    suspend fun update(face: Face)  = dao.update(face.toEntity())

    suspend fun delete(id: Long): Int {
        return dao.deleteById(id)
    }

    suspend fun delete(face: Face): Int {
        return dao.delete(face.toEntity())
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }


}
