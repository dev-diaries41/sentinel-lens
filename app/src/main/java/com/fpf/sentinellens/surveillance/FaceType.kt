package com.fpf.sentinellens.surveillance


enum class FaceType {BLACKLIST, WHITELIST}

val DetectionTypes = mapOf(
    FaceType.BLACKLIST to "Blacklist",
    FaceType.WHITELIST to "Whitelist"
)
