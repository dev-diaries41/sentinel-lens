package com.fpf.sentinellens.settings

import android.hardware.camera2.CameraCharacteristics

val frameIntervalOptions = mapOf(
    1000L to "1 second",
    2000L to "2 seconds",
    5000L to "5 seconds",
    10_000L to "10 seconds"
)

val maxDurationOptions = mapOf(
    60_000L to "1 minute",
    30 * 60_000L to "30 minutes",
    60 * 60_000L to "1 hour",
    6 * 60 * 60_000L to "6 hours",
    12 * 60 * 60_000L to "12 hours",
    24 * 60 * 60_000L to "1 day",
    null to "No limit"
)

val alertFrequencyOptions = mapOf(
    60_000L to "1 minute",
    5 * 60_000L to "5 minutes",
    15 * 60_000L to "15 minutes",
    30 * 60_000L to "30 minutes",
    60 * 60_000L to "1 hour",
)

val cameraOptions = mapOf(
    CameraCharacteristics.LENS_FACING_FRONT to "Front",
    CameraCharacteristics.LENS_FACING_BACK to "Back",
)