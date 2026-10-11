package com.fpf.sentinellens.ui.screens.watchlist

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class WatchlistViewModel(
    application: Application,
    private val repository: FacesRepository,
    ) : AndroidViewModel(application)  {
    val faceList: StateFlow<List<Face>> = repository.allFaces.stateIn(viewModelScope,
        SharingStarted.Lazily, emptyList())


    fun deleteFace(id: String){
        viewModelScope.launch {
            repository.delete(id)
            val filePath = "faces/${id.hashCode()}.jpg"
            val file = File(getApplication<Application>().filesDir, filePath)
            file.delete()
        }
    }
}