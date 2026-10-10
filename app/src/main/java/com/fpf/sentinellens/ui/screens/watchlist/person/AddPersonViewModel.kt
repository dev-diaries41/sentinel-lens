package com.fpf.sentinellens.ui.screens.watchlist.person

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.getBitmapFromUri
import com.fpf.sentinellens.lib.saveImageLocally
import com.fpf.smartscansdk.core.media.cropFaces
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class AddPersonViewModel(
    application: Application,
    private val repository: FacesRepository,
    private val faceComparer: InceptionResnetFaceEmbedder,
    private val faceDetector: FaceDetector
) : AndroidViewModel(application)  {

    private val _newName = MutableStateFlow("")
    val newName: StateFlow<String> = _newName

    private val _newFaceImage = MutableStateFlow<Uri?>(null)
    val newFaceImage: StateFlow<Uri?> = _newFaceImage

    private val _faceType = MutableStateFlow(FaceType.BLACKLIST)
    val faceType: StateFlow<FaceType> = _faceType

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isEditing = MutableStateFlow(false)

    fun updateName(name: String){
        _newName.value = name
    }

    fun updateFaceImage(uri: Uri){
        _error.value = null
        _newFaceImage.value = uri
    }

    fun updateDetectionType(type: FaceType){
        _faceType.value = type
    }

    fun reset(){
        _newName.value = ""
        _newFaceImage.value = null
    }

    fun onEditing(faceId: String){
        _isEditing.value = true
        viewModelScope.launch(Dispatchers.IO){
            val faceToEdit = repository.getFace(faceId)
            updateFaceImage(faceId.toUri())
            updateName(faceToEdit.name)
            updateDetectionType(faceToEdit.type)
        }
    }

    fun stopEditing(){
        _isEditing.value = false
    }

    fun addFace(){
        val name = _newName.value ?: return
        val newFaceImage = _newFaceImage.value ?: return
        val type = _faceType.value?: return

        viewModelScope.launch {
            try {
                if(!faceDetector.isInitialized()){
                    faceDetector.initialize()
                }
                if( !faceComparer.isInitialized()){
                    faceComparer.initialize()
                }
                val bitmap = getBitmapFromUri(getApplication(), newFaceImage)
                val (_, boxes) = faceDetector.detect(bitmap)
                val faces = cropFaces(bitmap, boxes)

                if(faces.isEmpty()) {
                    _error.value = "No faces detected"
                    return@launch
                }

                val faceEmbeddings = faceComparer.embed(faces[0])
                val filePath = "faces/${newFaceImage.toString().hashCode()}.jpg"
                val file = File(getApplication<Application>().filesDir, filePath)

                // TOOD: add error handling
                saveImageLocally(faces[0], file)

                val id = if(_isEditing.value) newFaceImage.toString() else file.toUri().toString()

                repository.insert(Face(
                    id = id,
                    date = System.currentTimeMillis(),
                    type = type,
                    name = name,
                    embeddings = faceEmbeddings
                ))

                reset()

            }catch (e: Exception){
                _error.value = "Error saving face image"
                Log.d("FacesViewModel", "Error adding face: $e")
            }
        }
    }
}