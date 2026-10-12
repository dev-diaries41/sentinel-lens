package com.fpf.sentinellens.ui.screens.watchlist.person

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.surveillance.Face
import com.fpf.sentinellens.surveillance.FaceType
import com.fpf.sentinellens.surveillance.NewFace
import com.fpf.sentinellens.surveillance.file
import com.fpf.smartscansdk.core.media.cropFaces
import com.fpf.smartscansdk.core.media.getBitmapFromUri
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AddPersonViewModel(
    application: Application,
    private val repository: FacesRepository,
    private val faceComparer: InceptionResnetFaceEmbedder,
    private val faceDetector: FaceDetector
) : AndroidViewModel(application)  {

    companion object {
        private const val FACE_DIR = "faces"
    }

    private val _state = MutableStateFlow(AddPersonState())
    val state: StateFlow<AddPersonState> = _state

    private val _error = MutableSharedFlow<String?>()
    val error = _error.asSharedFlow()

    fun onAction(action: AddPersonAction) = when(action){
        is AddPersonAction.SaveFace -> saveFace()
        is AddPersonAction.UpdateFaceImage -> updateFaceImage(action.uri)
        is AddPersonAction.UpdateFaceType -> updateDetectionType(action.type)
        is AddPersonAction.UpdateName -> updateName(action.name)
    }

    fun setFace(faceId: Long){
        viewModelScope.launch(Dispatchers.IO){
            val faceToEdit = repository.getFace(faceId)
            faceToEdit?.let{ face ->
                _state.update { it.copy(
                    faceToEdit = face,
                    name = face.name,
                    faceType = face.type,
                    faceImage = face.file(getApplication()).toUri())
                }
            }
        }
    }

    private fun updateName(name: String) = _state.update { it.copy(name = name) }
    private fun updateFaceImage(uri: Uri) = _state.update { it.copy(faceImage = uri) }
    private fun updateDetectionType(type: FaceType) = _state.update { it.copy(faceType = type) }
    private fun saveFace(){
        val currentState=  _state.value
        val name =currentState.name
        val faceImage =currentState.faceImage ?: return
        val type = currentState.faceType

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(!faceDetector.isInitialized()){
                    faceDetector.initialize()
                }
                if( !faceComparer.isInitialized()){
                    faceComparer.initialize()
                }
                val bitmap = getBitmapFromUri(getApplication(), faceImage)
                val (_, boxes) = faceDetector.detect(bitmap)
                val faces = cropFaces(bitmap, boxes)

                if(faces.isEmpty()) {
                    _error.emit ("No faces detected")
                    return@launch
                }

                val faceEmbeddings = faceComparer.embed(faces[0])

                if(currentState.faceToEdit != null){
                    val face = Face(
                        id = currentState.faceToEdit.id,
                        date = System.currentTimeMillis(),
                        type = type,
                        name = name,
                        embeddings = faceEmbeddings,
                        path = currentState.faceToEdit.path
                    )
                    repository.update(face)
                    saveImageLocally(faces[0], face.file(getApplication()))
                }else{
                    val path = createFilePath()
                    val face = NewFace(
                        date = System.currentTimeMillis(),
                        type = type,
                        name = name,
                        embeddings = faceEmbeddings,
                        path = path
                    )
                    repository.insert(face)
                    saveImageLocally(faces[0], face.file(getApplication()))
                }

                reset()

            }catch (e: Exception){
                _error.emit ("Error saving face image")
                Log.d("FacesViewModel", "Error adding face: $e")
            }
        }
    }

    private fun reset() = _state.update { AddPersonState() }

    private fun saveImageLocally(bitmap: Bitmap, file: File) {
        file.parentFile?.let { parentDir ->
            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }
        }
        file.outputStream().use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        }
    }

    private fun createFilePath(): String = "$FACE_DIR/${UUID.randomUUID()}.jpg"
}