package com.fpf.sentinellens.surveillance

data class DetectionLog(
    val id: Long,
    val date: Long,
    val type: FaceType,
    val name: String?,
    val similarity: Float
)

data class NewDetectionLog(
    val date: Long,
    val type: FaceType,
    val name: String?,
    val similarity: Float
)