package com.fpf.sentinellens.surveillance

data class Face(
    val id: Long,
    val date: Long,
    val embeddings: FloatArray,
    val type: FaceType,
    val name: String,
    val path: String
)

data class NewFace(
    val date: Long,
    val embeddings: FloatArray,
    val type: FaceType,
    val name: String,
    val path: String
)

