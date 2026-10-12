package com.fpf.sentinellens.surveillance

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

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

fun Face.file(context: Context): File = File(context.filesDir, path)
fun NewFace.file(context: Context): File = File(context.filesDir, path)
