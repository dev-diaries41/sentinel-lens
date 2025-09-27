package com.fpf.sentinellens.lib.camera

import android.app.Application
import android.media.Image
import android.util.Log
import com.fpf.sentinellens.R
import com.fpf.sentinellens.api.sendTelegramMessage
import com.fpf.sentinellens.data.faces.Face
import com.fpf.sentinellens.data.faces.FaceDatabase
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.faces.FaceType
import com.fpf.sentinellens.lib.cameraImageToBitmap
import com.fpf.sentinellens.lib.insertVideoIntoMediaStore
import com.fpf.sentinellens.lib.ml.FaceComparisonHelper
import com.fpf.sentinellens.lib.ml.FaceDetectorHelper
import com.fpf.sentinellens.lib.ml.cropFaces
import com.fpf.sentinellens.lib.showNotification
import com.fpf.smartscansdk.core.ml.embeddings.getSimilarities
import com.fpf.smartscansdk.core.ml.embeddings.getTopN
import com.fpf.smartscansdk.core.ml.models.ResourceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

interface IMlVideoCaptureListener : IVideoCaptureListener {
    fun closeSession()
}

class VideoCaptureListener(
    private val application: Application,
    private val alertFrequency: Long = 60 * 1000L,
    private val threshold: Float = 0.52F,
    private val telegramChannelId: String = "",
    private val telegramBotToken: String = "",
    private val mode: FaceType = FaceType.BLACKLIST
    ): IMlVideoCaptureListener
{
    private var faces: List<Face>? = null
    private var blackList: List<Face>? = null
    private var whiteList: List<Face>? = null
    private var lastDetectionTime: Long = 0L
    private var numDetections: Int = 0

    val faceComparer = FaceComparisonHelper(application.resources, ResourceId(R.raw.inception_resnet_v1_quant))
    val faceDetector= FaceDetectorHelper(application.resources, ResourceId(R.raw.face_detect))

    companion object {
        private const val TAG = "VideoCaptureListener"
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            faceDetector.initialize()
            faceComparer.initialize()
            val repository = FacesRepository(FaceDatabase.getDatabase(application).faceDao())
            faces = repository.getAllFacesSync()
            blackList = faces?.filter{it.type == FaceType.BLACKLIST }
            whiteList = faces?.filter{it.type == FaceType.WHITELIST }
        }
    }

    override fun onRecordingStarted() {
        Log.d(TAG, "Callback: Video recording has started")
    }

    override suspend fun onRecordingStopped(videoFilePath: String?) {
        if (videoFilePath == null) return

        val videoFile = File(videoFilePath)
        val videoUri = insertVideoIntoMediaStore(videoFile, application)

        if (videoUri != null) {
            videoFile.delete()
        } else {
            Log.e(TAG, "Failed to insert file into MediaStore.")
        }
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "Callback: Error in video recording", exception)
    }

    override suspend fun onFrame(image: Image, orientation: Int) {
        if (faces?.isEmpty() == true || !faceDetector.isInitialized() || !faceComparer.isInitialized()) return

        val bitmap = cameraImageToBitmap(image, orientation)
        val (_, boxes) = faceDetector.detect(bitmap)
        val faces = cropFaces(bitmap, boxes)

        if (faces.isEmpty()) return

        var detectedUnauthorisedPerson = false
        var unauthorisedPersonName = ""
        val detectedFacesEmbeddings = faces.map{faceComparer.embed(it)}

        if(!blackList.isNullOrEmpty() && mode == FaceType.BLACKLIST){
            var bestBlackListMatch = -1f

            detectedFacesEmbeddings.forEach{
                val blacklistSimilarities = getSimilarities(it, blackList!!.map{it.embeddings} )
                val bestIndex = getTopN(blacklistSimilarities, 1).first()
                val similarity = blacklistSimilarities[bestIndex]
                if(similarity > bestBlackListMatch){
                    bestBlackListMatch = similarity
                    unauthorisedPersonName = blackList!![bestIndex].name
                }
            }

            if(bestBlackListMatch >= threshold){
                detectedUnauthorisedPerson = true
            }
        }

        if(!whiteList.isNullOrEmpty() && mode == FaceType.WHITELIST){
            var bestWhiteListMatch = -1f

            detectedFacesEmbeddings.forEach{
                val whitelistSimilarities = getSimilarities(it, whiteList!!.map{it.embeddings} )
                val bestIndex = getTopN(whitelistSimilarities, 1).first()
                val similarity = whitelistSimilarities[bestIndex]
                if(similarity > bestWhiteListMatch){
                    bestWhiteListMatch = similarity
                }
            }
            // This means the detected person is not whitelisted
            // Whitelist is prone to false detection so multiple detections reduce false positives
            if(bestWhiteListMatch != -1f && bestWhiteListMatch < threshold){
                numDetections++
                if(numDetections >= 3){
                    detectedUnauthorisedPerson = true
                    numDetections = 0
                }
            }else{
                // A whitelisted person was accurately detected so recent
                numDetections = 0
            }
        }

        if(detectedUnauthorisedPerson){
            val currentDetectionTime = System.currentTimeMillis()

            if(currentDetectionTime - lastDetectionTime >= alertFrequency){
                lastDetectionTime = currentDetectionTime

                val title = application.getString(R.string.notif_detection_title)
                val messageTitle = if (unauthorisedPersonName.isNotEmpty()) "$title: $unauthorisedPersonName" else title

                if(telegramBotToken.isNotEmpty() && telegramChannelId.isNotEmpty()){
                    val result = sendTelegramMessage(telegramBotToken, messageTitle, telegramChannelId, bitmap)
                    result.onFailure { error ->
                        Log.e(TAG, "Error: $error")
                    }
                }else{
                    val baseContent = application.getString(R.string.notif_detection_content)
                    val content = if (unauthorisedPersonName.isNotEmpty()) "Detected person: $unauthorisedPersonName. $baseContent" else baseContent

                    showNotification(application, title, content)
                }
            }
        }
    }

    override fun closeSession(){
        faceComparer.closeSession()
        faceDetector.closeSession()
    }
}
