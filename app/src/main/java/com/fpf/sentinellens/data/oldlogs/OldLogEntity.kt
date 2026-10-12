package com.fpf.sentinellens.data.oldlogs

import androidx.room.*


@Entity(tableName = "detection_logs")
data class OldLogEntity(
    @PrimaryKey
    val id: String,
    val date: Long,
    val type: String,
    val name: String?,
    val similarity: Float
)