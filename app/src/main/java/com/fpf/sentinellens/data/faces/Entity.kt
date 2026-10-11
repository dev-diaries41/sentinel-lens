package com.fpf.sentinellens.data.faces

import androidx.room.*
import com.fpf.sentinellens.surveillance.FaceType

@Entity(tableName = "faces")
data class Face(
    @PrimaryKey
    val id: String,
    val date: Long,
    val embeddings: FloatArray,
    val type: FaceType,
    val name: String
)
