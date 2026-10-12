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
import com.fpf.sentinellens.utils.getBitmapFromUri
import com.fpf.smartscansdk.core.media.cropFaces
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

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

    private val _face = MutableStateFlow<Face?>(null)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


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
        _face.value = null
    }

    fun setFace(faceId: Long){
        viewModelScope.launch(Dispatchers.IO){
            val faceToEdit = repository.getFace(faceId)
            _face.value = faceToEdit
            updateFaceImage(faceToEdit.file(getApplication()).toUri())
            updateName(faceToEdit.name)
            updateDetectionType(faceToEdit.type)
        }
    }

    fun addFace(){
        val name = _newName.value
        val newFaceImage = _newFaceImage.value ?: return
        val type = _faceType.value

        viewModelScope.launch(Dispatchers.IO) {
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

                if(_face.value != null){
                    val face = Face(
                        id = _face.value?.id!!,
                        date = System.currentTimeMillis(),
                        type = type,
                        name = name,
                        embeddings = faceEmbeddings,
                        path = _face.value?.path!!
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
                _error.value = "Error saving face image"
                Log.d("FacesViewModel", "Error adding face: $e")
            }
        }
    }

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

    private fun createFilePath(): String = "faces/${UUID.randomUUID()}.jpg"
}