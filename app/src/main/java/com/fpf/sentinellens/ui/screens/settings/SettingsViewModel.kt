package com.fpf.sentinellens.ui.screens.settings

import android.app.Application
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.*
import com.fpf.sentinellens.lib.Storage
import com.fpf.sentinellens.lib.getBitmapFromUri
import com.fpf.sentinellens.lib.ml.FaceComparisonHelper
import com.fpf.sentinellens.lib.ml.FaceDetectorHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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

val modeOptions = mapOf(
    FaceType.BLACKLIST to "Blacklist",
    FaceType.WHITELIST to "Whitelist",
)

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

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = Storage.getInstance(getApplication())
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings

    init {
        loadSettings()
    }

    fun updateSimilarityThreshold(threshold: Float) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(similarityThreshold = threshold)
        saveSettings()
    }

    fun updateMode(mode: FaceType) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(mode = mode)
        saveSettings()
    }

    fun updateFrameInterval(interval: Long) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(frameInterval = interval)
        saveSettings()
    }

    fun updateMaxDuration(duration: Long?) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(maxDuration = duration)
        saveSettings()
    }

    fun updateCameraType(type: Int) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(cameraType = type)
        saveSettings()
    }

    fun updateAlertFrequency(duration: Long) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(alertFrequency = duration)
        saveSettings()
    }

    fun updateTelegramChannelId(channelId: String) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(telegramChannelId = channelId)
        saveSettings()
    }

    fun updateTelegramBotToken(token: String) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(telegramBotToken = token)
        saveSettings()
    }

    private fun loadSettings() {
        val jsonSettings = storage.getItem("app_settings")
        _appSettings.value = if (jsonSettings != null) {
            try {
                Json.decodeFromString<AppSettings>(jsonSettings)
            } catch (e: Exception) {
                Log.e("Settings", "Failed to decode settings", e)
                AppSettings()
            }
        } else {
            AppSettings()
        }
    }

    private fun saveSettings() {
        val jsonSettings = Json.encodeToString(_appSettings.value)
        storage.setItem("app_settings", jsonSettings)
    }
}
