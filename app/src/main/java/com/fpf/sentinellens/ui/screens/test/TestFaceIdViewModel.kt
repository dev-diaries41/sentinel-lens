package com.fpf.sentinellens.ui.screens.test

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.lib.getBitmapFromUri
import com.fpf.sentinellens.lib.ml.FaceComparisonHelper
import com.fpf.sentinellens.lib.ml.FaceDetectorHelper
import com.fpf.sentinellens.lib.ml.getSimilarities
import com.fpf.sentinellens.lib.ml.getTopN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestFaceIdViewModel(application: Application) : AndroidViewModel(application){
    val repository = FacesRepository(FaceDatabase.Companion.getDatabase(application).faceDao())
    val hasAnyFaces = repository.hasAnyFaces

    private var faceComparer: FaceComparisonHelper? = null
    private var faceDetector: FaceDetectorHelper? = null

    private val _blacklistSimilarity = MutableLiveData<Pair<Float, String>?>(null)
    val blacklistSimilarity: LiveData<Pair<Float, String>?> get() = _blacklistSimilarity

    private val _whitelistSimilarity = MutableLiveData<Pair<Float, String>?>(null)
    val whitelistSimilarity: LiveData<Pair<Float, String>?> get() = _whitelistSimilarity

    private val _selectedImage = MutableLiveData<Uri?>(null)
    val selectedImage: LiveData<Uri?> = _selectedImage

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _isLoading

    init {
        CoroutineScope(Dispatchers.Default).launch {
            faceComparer = FaceComparisonHelper(application.resources)
            faceDetector= FaceDetectorHelper(application.resources)
            _isLoading.postValue(false)
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
                if(faceDetector == null || faceComparer == null){
                    throw IllegalStateException("Models not loaded")
                }
                val croppedFaces = mutableListOf<Bitmap>()
                val uri = _selectedImage.value ?: return@launch
                val bitmap = getBitmapFromUri(getApplication(), uri)

                val (_, boxes) = faceDetector!!.detectFaces(bitmap)
                val faces = faceDetector!!.cropFaces(bitmap, boxes)
                croppedFaces.addAll(faces)

                val faceEmbeddings = croppedFaces.map{faceComparer!!.generateFaceEmbedding(it)}

                if(faceEmbeddings.isEmpty()) {
                    _error.postValue("No faces detected")
                    return@launch
                }

                if(faceEmbeddings.size > 1) {
                    _error.postValue("Invalid number of faces: ${faceEmbeddings.size}. Please ensure the image has only one person.")
                    return@launch
                }

                val allImageEmbeddings = repository.getAllFacesSync()
                val blacklist = allImageEmbeddings.filter{it.type == FaceType.BLACKLIST }
                val whitelist = allImageEmbeddings.filter{it.type == FaceType.WHITELIST }

                if(blacklist.isNotEmpty()){
                    val blacklistSimilarities = getSimilarities(faceEmbeddings[0], blacklist.map { it.embeddings })
                    val bestBlacklistIndex = getTopN(blacklistSimilarities, 1).first()
                    val result = Pair(blacklistSimilarities[bestBlacklistIndex], blacklist[bestBlacklistIndex].name)
                    _blacklistSimilarity.postValue(result)
                }

                if(whitelist.isNotEmpty()){
                    val whitelistSimilarities = getSimilarities(faceEmbeddings[0], whitelist.map { it.embeddings })
                    val bestWhitelistIndex = getTopN(whitelistSimilarities, 1).first()
                    val result = Pair(whitelistSimilarities[bestWhitelistIndex], whitelist[bestWhitelistIndex].name)
                    _whitelistSimilarity.postValue(result)
                }
            } catch (e: Exception) {
                Log.e("FaceIdViewModel", "Inference failed: ${e.message}", e)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        faceComparer?.closeSession()
        faceDetector?.closeSession()
    }
}