package com.fpf.sentinellens.data.faces

import androidx.room.*
import com.fpf.sentinellens.surveillance.FaceType

@Entity(tableName = "faces")
data class FaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: Long,
    val embeddings: FloatArray,
    val type: FaceType,
    val name: String,
    val path: String
)
