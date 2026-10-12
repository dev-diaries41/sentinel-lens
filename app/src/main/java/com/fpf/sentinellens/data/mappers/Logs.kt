package com.fpf.sentinellens.data.mappers

import com.fpf.sentinellens.data.logs.LogEntity
import com.fpf.sentinellens.surveillance.DetectionLog
import com.fpf.sentinellens.surveillance.NewDetectionLog

fun LogEntity.toDomain(): DetectionLog = DetectionLog(
    id=id,
    date=date,
    type = type,
    similarity=similarity,
    name = name
)

fun DetectionLog.toEntity(): LogEntity = LogEntity(
    id=id,
    date=date,
    type = type,
    similarity=similarity,
    name = name
)

fun NewDetectionLog.toEntity(): LogEntity = LogEntity(
    date=date,
    type = type,
    similarity=similarity,
    name = name
)