package com.fpf.sentinellens.settings

import android.hardware.camera2.CameraCharacteristics
import com.fpf.sentinellens.data.faces.FaceType
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val similarityThreshold: Float = 0.52f,
    val mode: FaceType = FaceType.BLACKLIST,
    val frameInterval: Long = 1000L,
    val alertFrequency: Long = 60_000L,
    val maxDuration: Long? = null,
    val cameraType: Int = CameraCharacteristics.LENS_FACING_BACK,
    val telegramChannelId: String = "",
    val telegramBotToken: String = ""
)
