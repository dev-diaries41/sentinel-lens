package com.fpf.sentinellens.data.faces

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FacesDao {

    @Query("SELECT * FROM faces ORDER BY date DESC")
    fun getAllFaces(): Flow<List<Face>>

    @Query("SELECT EXISTS(SELECT 1 FROM faces)")
    fun hasAnyFaces(): Flow<Boolean>

    @Query("SELECT * FROM faces WHERE id=:id")
    suspend fun getFace(id: String): Face

    @Query("SELECT * FROM faces ORDER BY date DESC")
    suspend fun getAllFacesSync(): List<Face>

    @Query("UPDATE faces SET id = :newId WHERE id = :oldId")
    suspend fun updateFaceId(oldId: String, newId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFace(face: Face)

    @Query("DELETE FROM faces WHERE id = :id")
    suspend fun deleteFace(id: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM faces WHERE id = :id)")
    suspend fun isExist(id: String): Boolean

    @Query("DELETE FROM faces")
    suspend fun deleteAll()
}
