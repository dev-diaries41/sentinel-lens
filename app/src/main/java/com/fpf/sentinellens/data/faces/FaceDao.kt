package com.fpf.sentinellens.data.faces

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FacesDao {

    @Query("SELECT * FROM faces ORDER BY date DESC")
    fun getAllFaces(): Flow<List<FaceEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM faces)")
    fun hasAnyFaces(): Flow<Boolean>

    @Query("SELECT * FROM faces WHERE id=:id")
    suspend fun getById(id: Long): FaceEntity

    @Query("SELECT * FROM faces ORDER BY date DESC")
    suspend fun getAllFacesSync(): List<FaceEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(face: FaceEntity)

    @Update
    suspend fun update(face: FaceEntity)

    @Update
    suspend fun update(faces: List<FaceEntity>)

    @Query("DELETE FROM faces WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Delete
    suspend fun delete(face: FaceEntity): Int

    @Query("DELETE FROM faces")
    suspend fun deleteAll()
}
