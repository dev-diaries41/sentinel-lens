package com.fpf.sentinellens.ui.screens.surveillance

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.fpf.sentinellens.data.faces.DetectionTypes
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.services.SurveillanceForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SurveillanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FacesRepository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())
    val hasAnyFaces = repository.hasAnyFaces

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions

    private val _isSurveillanceActive = MutableStateFlow(false)
    val isSurveillanceActive: StateFlow<Boolean> = _isSurveillanceActive

    private val _detectionMode = MutableStateFlow(FaceType.BLACKLIST)
    val detectionMode: StateFlow<FaceType> = _detectionMode

    fun checkPermissions(notifications: Boolean, camera: Boolean) {
        if ( camera) {
            _hasPermissions.value = true
        }else{
            Toast.makeText(getApplication(), "Missing camera permission", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateDetectionMode(mode: FaceType){
        _detectionMode.value = mode
    }

    fun startSurveillance() {
        if (_hasPermissions.value) {
            _isSurveillanceActive.value = true
            Intent(getApplication(), SurveillanceForegroundService::class.java).putExtra(SurveillanceForegroundService.MODE, _detectionMode.value
            ).also{
                intent -> getApplication<Application>().startForegroundService(intent)
            }
        }
    }

    @SuppressLint("ImplicitSamInstance")
    fun stopSurveillance() {
        _isSurveillanceActive.value = false
        getApplication<Application>().stopService(Intent(getApplication(), SurveillanceForegroundService::class.java))
    }
}
