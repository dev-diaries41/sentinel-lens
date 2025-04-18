package com.fpf.sentinellens.ui.screens.surveillance

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.services.SurveillanceForegroundService

class SurveillanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FacesRepository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())
    val hasAnyFaces = repository.hasAnyFaces

    private val _hasPermissions = MutableLiveData<Boolean>(false)
    val hasPermissions: LiveData<Boolean> = _hasPermissions

    private val _isSurveillanceActive = MutableLiveData<Boolean>(false)
    val isSurveillanceActive: LiveData<Boolean> = _isSurveillanceActive

    fun checkPermissions(notifications: Boolean, camera: Boolean) {
        if ( camera) {
            _hasPermissions.value = true
        }else{
            Toast.makeText(getApplication<Application>(), "Missing camera permission", Toast.LENGTH_SHORT).show()
        }
    }

    fun startSurveillance() {
        if (_hasPermissions.value == true) {
            _isSurveillanceActive.value = true
            getApplication<Application>().startForegroundService(Intent(getApplication<Application>(), SurveillanceForegroundService::class.java))
        }
    }

    @SuppressLint("ImplicitSamInstance")
    fun stopSurveillance() {
        _isSurveillanceActive.value = false
        getApplication<Application>().stopService(Intent(getApplication<Application>(), SurveillanceForegroundService::class.java))
    }
}
