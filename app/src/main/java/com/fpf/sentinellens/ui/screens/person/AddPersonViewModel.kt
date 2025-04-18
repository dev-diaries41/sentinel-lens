package com.fpf.sentinellens.ui.screens.person

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.getBitmapFromUri
import com.fpf.sentinellens.lib.ml.FaceComparisonHelper
import com.fpf.sentinellens.lib.ml.FaceDetectorHelper
import com.fpf.sentinellens.lib.saveImageLocally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddPersonViewModel(application: Application) : AndroidViewModel(application)  {
    private var faceComparer: FaceComparisonHelper? = null
    private var faceDetector: FaceDetectorHelper? = null

    private val repository: FacesRepository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())

    private val _newName =  MutableLiveData<String>("")
    val newName:LiveData<String> = _newName

    private val _newFaceImage = MutableLiveData<Uri?>(null)
    val newFaceImage: LiveData<Uri?> = _newFaceImage

    private val _faceType = MutableLiveData<FaceType>(FaceType.BLACKLIST)
    val faceType: LiveData<FaceType> = _faceType

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error


    init {
        CoroutineScope(Dispatchers.Default).launch {
            faceComparer = FaceComparisonHelper(application.resources)
            faceDetector= FaceDetectorHelper(application.resources)
        }
    }

    fun updateName(name: String){
        _newName.value = name
    }

    fun updateFaceImage(uri: Uri){
        _error.value = null
        _newFaceImage.value = uri
    }

    fun updateFaceType(type: FaceType){
        _faceType.value = type
    }

    fun reset(){
        _newName.postValue("")
        _newFaceImage.postValue(null)
    }

    fun addFace(name: String, newFaceImage: Uri, type: FaceType){
        viewModelScope.launch {
            try {
                if(faceDetector == null || faceComparer == null){
                    throw IllegalStateException("Models not loaded")
                }
                val bitmap = getBitmapFromUri(getApplication(), newFaceImage)
                val (_, boxes) = faceDetector!!.detectFaces(bitmap)
                val faces = faceDetector!!.cropFaces(bitmap, boxes)

                if(faces.isEmpty()) {
                    _error.postValue("No faces detected")
                    return@launch
                }

                val faceEmbeddings = faceComparer!!.generateFaceEmbedding(faces[0])
                val filePath = "faces/${newFaceImage.toString().hashCode()}.jpg"
                val saved = saveImageLocally(getApplication(), faces[0], filePath)

                if (!saved) {
                    _error.postValue("Error saving face image")
                    return@launch
                }

                repository.insert(Face(
                    id = newFaceImage.toString(),
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