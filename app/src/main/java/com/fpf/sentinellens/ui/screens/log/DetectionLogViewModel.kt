package com.fpf.sentinellens.ui.screens.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.logs.LogRepository
import com.fpf.sentinellens.surveillance.DetectionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetectionLogViewModel(
    application: Application,
    private val detectionLogRepository: LogRepository
    ) : AndroidViewModel(application) {
    val log: StateFlow<List<DetectionLog>> = detectionLogRepository.getLogsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isClearLogsAlertVisible = MutableStateFlow(false)
    val isClearLogsAlertVisible: StateFlow<Boolean> = _isClearLogsAlertVisible

    fun clearLogs(){
        viewModelScope.launch(Dispatchers.IO) {
            detectionLogRepository.clear()
        }
    }

    fun toggleAlert(){
        _isClearLogsAlertVisible.value = !_isClearLogsAlertVisible.value
    }
}