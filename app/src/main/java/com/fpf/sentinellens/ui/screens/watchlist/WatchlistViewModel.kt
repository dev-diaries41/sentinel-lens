package com.fpf.sentinellens.ui.screens.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class WatchlistViewModel(application: Application) : AndroidViewModel(application)  {
    private val repository: FacesRepository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())
    val faceList: StateFlow<List<Face>> = repository.allFaces.stateIn(viewModelScope,
        SharingStarted.Lazily, emptyList())

    val storage = Storage.getInstance(application)

    init {
        val hasUpdatedIds = storage.getItem("hasUpdatedIds") != null
        if(!hasUpdatedIds){
            viewModelScope.launch(Dispatchers.IO){
                repository.migratePickerUrisToLocal(application) { storage.setItem("hasUpdatedIds", true.toString()) }
            }
        }
    }

    fun deleteFace(id: String){
        viewModelScope.launch {
            repository.delete(id)
            val filePath = "faces/${id.hashCode()}.jpg"
            val file = File(getApplication<Application>().filesDir, filePath)
            file.delete()
        }
    }
}