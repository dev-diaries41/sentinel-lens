package com.fpf.sentinellens.data.mappers

import com.fpf.sentinellens.data.faces.FaceEntity
import com.fpf.sentinellens.surveillance.Face
import com.fpf.sentinellens.surveillance.NewFace

fun FaceEntity.toDomain(): Face = Face(
    id=id,
    name=name,
    path = path,
    date = date,
    embeddings = embeddings,
    type = type
)

fun Face.toEntity(): FaceEntity = FaceEntity(
    id=id,
    name=name,
    path = path,
    date = date,
    embeddings = embeddings,
    type = type
)

fun NewFace.toEntity(): FaceEntity = FaceEntity(
    name=name,
    path = path,
    date = date,
    embeddings = embeddings,
    type = type
)