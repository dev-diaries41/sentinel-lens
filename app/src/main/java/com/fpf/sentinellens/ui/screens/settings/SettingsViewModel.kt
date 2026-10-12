package com.fpf.sentinellens.ui.screens.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import com.fpf.sentinellens.settings.AppSettings
import com.fpf.sentinellens.settings.loadSettings
import com.fpf.sentinellens.settings.saveSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    application: Application,
    private val sharedPrefs: SharedPreferences,
) : AndroidViewModel(application) {
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings

    init {
        _appSettings.value = loadSettings(sharedPrefs)
    }

    fun onAction(action: SettingAction) = when(action){
        is SettingAction.UpdateAlertFrequency -> updateAlertFrequency(action.frequency)
        is SettingAction.UpdateCameraType -> updateCameraType(action.type)
        is SettingAction.UpdateFrameInterval -> updateFrameInterval(action.interval)
        is SettingAction.UpdateMaxDuration -> updateMaxDuration(action.duration)
        is SettingAction.UpdateSimilarityThreshold -> updateSimilarityThreshold(action.threshold)
        is SettingAction.UpdateTelegramBotToken -> updateTelegramBotToken(action.token)
        is SettingAction.UpdateTelegramChannelId -> updateTelegramChannelId(action.channelId)
    }

    private fun updateSimilarityThreshold(threshold: Float) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(similarityThreshold = threshold)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    private fun updateFrameInterval(interval: Long) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(frameInterval = interval)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    private fun updateMaxDuration(duration: Long?) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(maxDuration = duration)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    private fun updateCameraType(type: Int) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(cameraType = type)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    private fun updateAlertFrequency(frequency: Long) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(alertFrequency = frequency)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    private fun updateTelegramChannelId(channelId: String) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(telegramChannelId = channelId)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    private fun updateTelegramBotToken(token: String) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(telegramBotToken = token)
        saveSettings(sharedPrefs, _appSettings.value)
    }
}
