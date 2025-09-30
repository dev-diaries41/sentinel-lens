package com.fpf.sentinellens.ui.screens.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.logs.DetectionLogDatabase
import com.fpf.sentinellens.data.logs.DetectionLogEntity
import com.fpf.sentinellens.data.logs.DetectionLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetectionLogViewModel(application: Application) : AndroidViewModel(application) {
    private val detectionLogRepository: DetectionLogRepository = DetectionLogRepository(DetectionLogDatabase.getDatabase(application).detectionLogsDao())
    val log: StateFlow<List<DetectionLogEntity>> = detectionLogRepository.allScanData.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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