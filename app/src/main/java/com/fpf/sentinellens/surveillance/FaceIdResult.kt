package com.fpf.sentinellens.surveillance

data class FaceIdResult(
    val isMatch: Boolean,
    val name: String,
    val similarity: Float
)