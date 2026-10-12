package com.fpf.sentinellens.ui.screens.test

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.surveillance.FaceIdResult
import com.fpf.sentinellens.surveillance.FaceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.fpf.smartscansdk.core.embeddings.getSimilarities
import com.fpf.smartscansdk.core.embeddings.getTopN
import com.fpf.smartscansdk.core.media.cropFaces
import com.fpf.smartscansdk.core.media.drawBoxes
import com.fpf.smartscansdk.core.media.getBitmapFromUri
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TestFaceIdViewModel(
    application: Application,
    private val repository: FacesRepository,
    private val faceComparer: InceptionResnetFaceEmbedder,
    private val faceDetector: FaceDetector
) : AndroidViewModel(application){

    companion object {
        private const val MIN_SIMILARITY: Float = 0.51f
        private const val BOTTOM_SIM_RANGE = -1f
    }

    val hasAnyFaces: StateFlow<Boolean?> = repository.hasAnyFaces.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _state = MutableStateFlow(TestFaceIdState())
    val state: StateFlow<TestFaceIdState> = _state

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    fun updateImageUri(uri: Uri) = _state.update { it.copy(selectedImage = uri) }

    fun reset() = _state.update { TestFaceIdState() }

    fun run() {
        _state.update { it.copy(loading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(!faceDetector.isInitialized()){
                    faceDetector.initialize()
                }
                if( !faceComparer.isInitialized()){
                    faceComparer.initialize()
                }
                val croppedFaces = mutableListOf<Bitmap>()
                val uri = _state.value.selectedImage ?: return@launch
                val bitmap = getBitmapFromUri(getApplication(), uri)

                val (_, boxes) = faceDetector.detect(bitmap)
                val faces = cropFaces(bitmap, boxes)
                croppedFaces.addAll(faces)

                val faceEmbeddings = croppedFaces.map{faceComparer.embed(it)}

                if(faceEmbeddings.isEmpty()) {
                    _error.emit("No faces detected")
                    return@launch
                }

                val drawnBoxes = drawBoxes(bitmap, boxes, Color.GREEN)
                _state.update { it.copy(selectedImageBoundedBoxes = drawnBoxes) }

                val allImageEmbeddings = repository.getAllFacesSync()
                val blacklist = allImageEmbeddings.filter{it.type == FaceType.BLACKLIST }
                val whitelist = allImageEmbeddings.filter{it.type == FaceType.WHITELIST }

                if(blacklist.isNotEmpty()){
                    val blacklistSimilarities = getSimilarities(faceEmbeddings[0], blacklist.map { it.embeddings })
                    val bestBlacklistIndex = getTopN(blacklistSimilarities, 1, threshold = BOTTOM_SIM_RANGE).first()
                    val sim = blacklistSimilarities[bestBlacklistIndex]
                    val name = blacklist[bestBlacklistIndex].name
                    val result = FaceIdResult(
                        isMatch = sim >= MIN_SIMILARITY,
                        name=name,
                        similarity = sim
                    )
                    _state.update { it.copy(blacklistResult = result) }
                }

                if(whitelist.isNotEmpty()){
                    val whitelistSimilarities = getSimilarities(faceEmbeddings[0], whitelist.map { it.embeddings })
                    val bestWhitelistIndex = getTopN(whitelistSimilarities, 1, threshold = BOTTOM_SIM_RANGE).first()
                    val sim = whitelistSimilarities[bestWhitelistIndex]
                    val name = whitelist[bestWhitelistIndex].name
                    val result = FaceIdResult(
                        isMatch = sim >= MIN_SIMILARITY,
                        name=name,
                        similarity = sim
                    )
                    _state.update { it.copy(whitelistResult = result) }
                }
            } catch (e: Exception) {
                Log.e("FaceIdViewModel", "Inference failed: ${e.message}", e)
                _error.emit("Error testing face id")
            }finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}