package com.fpf.sentinellens.ui.screens.settings

sealed interface SettingAction {
    data class UpdateSimilarityThreshold(val threshold: Float): SettingAction
    data class UpdateFrameInterval(val interval: Long): SettingAction
    data class UpdateMaxDuration(val duration: Long?): SettingAction
    data class UpdateCameraType(val type: Int): SettingAction
    data class UpdateAlertFrequency(val frequency: Long): SettingAction
    data class UpdateTelegramChannelId(val channelId: String): SettingAction
    data class UpdateTelegramBotToken(val token: String): SettingAction
}