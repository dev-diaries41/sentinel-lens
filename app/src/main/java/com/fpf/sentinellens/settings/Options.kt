package com.fpf.sentinellens.settings

import android.hardware.camera2.CameraCharacteristics

val frameIntervalOptions: List<Pair<String, Long>> = listOf(
    "1 second" to 1_000L,
    "2 seconds" to 2_000L,
    "5 seconds" to 5_000L,
    "10 seconds" to 10_000L
)

val maxDurationOptions: List<Pair<String, Long?>> = listOf(
    "1 minute" to 60_000L,
    "30 minutes" to 30 * 60_000L,
    "1 hour" to 60 * 60_000L,
    "6 hours" to 6 * 60 * 60_000L,
    "12 hours" to 12 * 60 * 60_000L,
    "1 day" to 24 * 60 * 60_000L,
    "No limit" to null
)

val alertFrequencyOptions: List<Pair<String, Long>> = listOf(
    "1 minute" to 60_000L,
    "5 minutes" to 5 * 60_000L,
    "15 minutes" to 15 * 60_000L,
    "30 minutes" to 30 * 60_000L,
    "1 hour" to 60 * 60_000L
)

val cameraOptions: List<Pair<String, Int>> = listOf(
    "Front" to CameraCharacteristics.LENS_FACING_FRONT,
    "Back" to CameraCharacteristics.LENS_FACING_BACK
)

fun <T>getLabel(value: T, options: List<Pair<String, T>>, default: String = ""): String = options.find { it.second == value }?.first?: default