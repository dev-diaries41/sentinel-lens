package com.fpf.sentinellens.data.faces

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FacesDao {

    @Query("SELECT * FROM faces ORDER BY date DESC")
    fun getAllFaces(): LiveData<List<Face>>

    @Query("SELECT EXISTS(SELECT 1 FROM faces)")
    fun hasAnyFaces(): LiveData<Boolean>

    @Query("SELECT * FROM faces ORDER BY date DESC")
    suspend fun getAllFacesSync(): List<Face>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFace(face: Face)

    @Query("DELETE FROM faces WHERE id = :id")
    suspend fun deleteFace(id: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM faces WHERE id = :id)")
    suspend fun isExist(id: String): Boolean

    @Query("DELETE FROM faces")
    suspend fun deleteAll()
}
