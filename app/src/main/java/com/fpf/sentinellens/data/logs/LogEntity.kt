package com.fpf.sentinellens.data.logs

import androidx.room.*
import com.fpf.sentinellens.surveillance.FaceType

@Entity(tableName = "detection_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: Long,
    val type: FaceType,
    val name: String?,
    val similarity: Float
)