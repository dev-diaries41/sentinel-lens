package com.fpf.sentinellens.ui.screens.watchlist.person

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.R
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.getBitmapFromUri
import com.fpf.sentinellens.lib.ml.FaceComparisonHelper
import com.fpf.sentinellens.lib.ml.FaceDetectorHelper
import com.fpf.sentinellens.lib.ml.cropFaces
import com.fpf.sentinellens.lib.saveImageLocally
import com.fpf.smartscansdk.core.ml.models.ResourceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AddPersonViewModel(application: Application) : AndroidViewModel(application)  {
    val faceComparer = FaceComparisonHelper(application.resources, ResourceId(R.raw.inception_resnet_v1_quant))
    val faceDetector= FaceDetectorHelper(application.resources, ResourceId(R.raw.face_detect))

    private val repository: FacesRepository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())

    private val _newName =  MutableLiveData("")
    val newName:LiveData<String> = _newName

    private val _newFaceImage = MutableLiveData<Uri?>(null)
    val newFaceImage: LiveData<Uri?> = _newFaceImage

    private val _faceType = MutableLiveData(FaceType.BLACKLIST)
    val faceType: LiveData<FaceType> = _faceType

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _isEditing = MutableStateFlow(false)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            faceComparer.initialize()
            faceDetector.initialize()
        }
    }

    fun updateName(name: String){
        _newName.postValue(name)
    }

    fun updateFaceImage(uri: Uri){
        _error.postValue(null)
        _newFaceImage.postValue(uri)
    }

    fun updateDetectionType(type: FaceType){
        _faceType.postValue(type)
    }

    fun reset(){
        _newName.postValue("")
        _newFaceImage.postValue(null)
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
                if(!faceDetector.isInitialized() || !faceComparer.isInitialized()){
                    throw IllegalStateException("Models not loaded")
                }
                val bitmap = getBitmapFromUri(getApplication(), newFaceImage)
                val (_, boxes) = faceDetector.detect(bitmap)
                val faces = cropFaces(bitmap, boxes)

                if(faces.isEmpty()) {
                    _error.postValue("No faces detected")
                    return@launch
                }

                val faceEmbeddings = faceComparer.embed(faces[0])
                val filePath = "faces/${newFaceImage.toString().hashCode()}.jpg"
                val file = File(getApplication<Application>().filesDir, filePath)
                val saved = saveImageLocally(getApplication(), faces[0], file)

                if (saved) {
                    _error.postValue("Error saving face image")
                    return@launch
                }

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
                Log.d("FacesViewModel", "Error adding face: $e")
            }
        }
    }
}