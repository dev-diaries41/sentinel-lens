package com.fpf.sentinellens.data.logs

import androidx.room.*


@Entity(tableName = "detection_logs")
data class DetectionLogEntity(
    @PrimaryKey
    val id: String,
    val faceId: String,
    val date: Long,
    val type: String,
    val name: String,
    val similarity: Float
)