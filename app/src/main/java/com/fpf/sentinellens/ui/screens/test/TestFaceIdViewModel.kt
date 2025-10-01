package com.fpf.sentinellens.ui.screens.test

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.getBitmapFromUri
import com.fpf.sentinellens.lib.ml.FaceEmbedder
import com.fpf.sentinellens.lib.ml.FaceDetector
import com.fpf.sentinellens.lib.ml.cropFaces
import com.fpf.smartscansdk.core.ml.embeddings.getSimilarities
import com.fpf.smartscansdk.core.ml.embeddings.getTopN
import com.fpf.smartscansdk.core.ml.models.ResourceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fpf.sentinellens.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TestFaceIdViewModel(application: Application) : AndroidViewModel(application){
    val faceComparer = FaceEmbedder(application.resources, ResourceId(R.raw.inception_resnet_v1_quant))
    val faceDetector= FaceDetector(application.resources, ResourceId(R.raw.face_detect))

    val repository = FacesRepository(FaceDatabase.Companion.getDatabase(application).faceDao())
    val hasAnyFaces: StateFlow<Boolean?> = repository.hasAnyFaces.stateIn(viewModelScope, SharingStarted.Lazily, null)


    private val _blacklistSimilarity = MutableStateFlow<Pair<Float, String>?>(null)
    val blacklistSimilarity: StateFlow<Pair<Float, String>?> get() = _blacklistSimilarity

    private val _whitelistSimilarity = MutableStateFlow<Pair<Float, String>?>(null)
    val whitelistSimilarity: StateFlow<Pair<Float, String>?> get() = _whitelistSimilarity

    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImage: StateFlow<Uri?> = _selectedImage

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _isLoading

    init {
        CoroutineScope(Dispatchers.Default).launch {
            faceComparer.initialize()
            faceDetector.initialize()
            _isLoading.emit(false)
        }
    }

    fun updateImageUri(uri: Uri?) {
        _error.value = null
        _selectedImage.value = uri
    }

    fun clearInferenceResult() {
        _error.value = null
        _blacklistSimilarity.value = null
        _whitelistSimilarity.value = null
    }

    fun inference() {
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(!faceDetector.isInitialized() || !faceComparer.isInitialized()){
                    _error.emit("Models not loaded")
                    return@launch
                }
                val croppedFaces = mutableListOf<Bitmap>()
                val uri = _selectedImage.value ?: return@launch
                val bitmap = getBitmapFromUri(getApplication(), uri)

                val (_, boxes) = faceDetector.detect(bitmap)
                val faces = cropFaces(bitmap, boxes)
                croppedFaces.addAll(faces)

                val faceEmbeddings = croppedFaces.map{faceComparer.embed(it)}

                if(faceEmbeddings.isEmpty()) {
                    _error.emit("No faces detected")
                    return@launch
                }

                if(faceEmbeddings.size > 1) {
                    _error.emit("Invalid number of faces: ${faceEmbeddings.size}. Please ensure the image has only one person.")
                    return@launch
                }

                val allImageEmbeddings = repository.getAllFacesSync()
                val blacklist = allImageEmbeddings.filter{it.type == FaceType.BLACKLIST }
                val whitelist = allImageEmbeddings.filter{it.type == FaceType.WHITELIST }

                if(blacklist.isNotEmpty()){
                    val blacklistSimilarities = getSimilarities(faceEmbeddings[0], blacklist.map { it.embeddings })
                    val bestBlacklistIndex = getTopN(blacklistSimilarities, 1).first()
                    val result = Pair(blacklistSimilarities[bestBlacklistIndex], blacklist[bestBlacklistIndex].name)
                    _blacklistSimilarity.emit(result)
                }

                if(whitelist.isNotEmpty()){
                    val whitelistSimilarities = getSimilarities(faceEmbeddings[0], whitelist.map { it.embeddings })
                    val bestWhitelistIndex = getTopN(whitelistSimilarities, 1).first()
                    val result = Pair(whitelistSimilarities[bestWhitelistIndex], whitelist[bestWhitelistIndex].name)
                    _whitelistSimilarity.emit(result)
                }
            } catch (e: Exception) {
                Log.e("FaceIdViewModel", "Inference failed: ${e.message}", e)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        faceComparer.closeSession()
        faceDetector.closeSession()
    }
}