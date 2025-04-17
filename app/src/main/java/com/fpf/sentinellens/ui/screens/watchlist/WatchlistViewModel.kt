package com.fpf.sentinellens.ui.screens.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.deleteLocalFile
import kotlinx.coroutines.launch

class WatchlistViewModel(application: Application) : AndroidViewModel(application)  {
    private val repository: FacesRepository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())
    val faceList: LiveData<List<Face>> =  repository.allFaces

    fun deleteFace(id: String){
        viewModelScope.launch {
            repository.delete(id)
            val filePath = "faces/${id.hashCode()}.jpg"
            deleteLocalFile(getApplication(), filePath)
        }
    }
}