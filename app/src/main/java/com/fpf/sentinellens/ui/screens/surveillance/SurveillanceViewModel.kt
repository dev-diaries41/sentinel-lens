package com.fpf.sentinellens.ui.screens.surveillance

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.services.SurveillanceForegroundService
import com.fpf.sentinellens.surveillance.FaceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SurveillanceViewModel(
    application: Application,
    private val repository: FacesRepository
) : AndroidViewModel(application) {
    val hasAnyFaces: StateFlow<Boolean?> = repository.hasAnyFaces.stateIn(viewModelScope, SharingStarted.Lazily, null)
    private val _isSurveillanceActive = MutableStateFlow(false)
    val isSurveillanceActive: StateFlow<Boolean> = _isSurveillanceActive
    private val _detectionMode = MutableStateFlow(FaceType.BLACKLIST)
    val detectionMode: StateFlow<FaceType> = _detectionMode

    fun updateDetectionMode(mode: FaceType){
        _detectionMode.value = mode
    }

    fun startSurveillance() {
        _isSurveillanceActive.value = true
        Intent(getApplication(), SurveillanceForegroundService::class.java)
            .putExtra(SurveillanceForegroundService.MODE, _detectionMode.value)
            .also{ intent -> getApplication<Application>().startForegroundService(intent) }
    }

    @SuppressLint("ImplicitSamInstance")
    fun stopSurveillance() {
        _isSurveillanceActive.value = false
        getApplication<Application>().stopService(Intent(getApplication(), SurveillanceForegroundService::class.java))
    }
}
