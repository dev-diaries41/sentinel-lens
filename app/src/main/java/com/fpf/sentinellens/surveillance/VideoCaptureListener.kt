package com.fpf.sentinellens.surveillance

import android.app.Application
import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import com.fpf.sentinellens.R
import com.fpf.sentinellens.api.sendTelegramMessage
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.logs.LogRepository
import com.fpf.sentinellens.notifications.NotificationChannels
import com.fpf.sentinellens.notifications.showNotification
import com.fpf.sentinellens.utils.cameraImageToBitmap
import com.fpf.sentinellens.utils.insertVideoIntoMediaStore
import com.fpf.smartscansdk.core.embeddings.getSimilarities
import com.fpf.smartscansdk.core.embeddings.getTopN
import com.fpf.smartscansdk.core.media.cropFaces
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class VideoCaptureListener(
    private val application: Application,
    private val alertFrequency: Long = 60 * 1000L,
    private val threshold: Float = 0.52F,
    private val telegramChannelId: String = "",
    private val telegramBotToken: String = "",
    private val mode: FaceType = FaceType.BLACKLIST,
    private val faceComparer: InceptionResnetFaceEmbedder,
    private val faceDetector: FaceDetector,
    private val faceRepository: FacesRepository,
    private val detectionLogRepository: LogRepository
): IVideoCaptureListener
{
    private var blackList: List<Face>? = null
    private var whiteList: List<Face>? = null
    private var lastDetectionTime: Long = 0L
    private var numDetections: Int = 0

    companion object {
        private const val TAG = "VideoCaptureListener"
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            setupListener()
        }
    }

    private suspend fun setupListener(){
        if(!faceDetector.isInitialized()){
            faceDetector.initialize()
        }
        if( !faceComparer.isInitialized()){
            faceComparer.initialize()
        }
        val storedFaces = faceRepository.getAllFacesSync()
        blackList = storedFaces.filter{it.type == FaceType.BLACKLIST }
        whiteList = storedFaces.filter{it.type == FaceType.WHITELIST }
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
        if ((blackList.isNullOrEmpty() && whiteList.isNullOrEmpty()) || !faceDetector.isInitialized() || !faceComparer.isInitialized()) return

        val bitmap = cameraImageToBitmap(image, orientation)
        val (_, boxes) = faceDetector.detect(bitmap)
        val croppedFaces = cropFaces(bitmap, boxes)

        if (croppedFaces.isEmpty()) return

        var detectedUnauthorisedPerson = false
        var unauthorisedPersonName: String? = null
        val detectedFacesEmbeddings = croppedFaces.map{faceComparer.embed(it)}
        var bestMatch = -1f

        if(!blackList.isNullOrEmpty() && mode == FaceType.BLACKLIST){
            for(faceEmbedding in detectedFacesEmbeddings){
                val blacklistSimilarities = getSimilarities(faceEmbedding, blackList!!.map{it.embeddings} )
                val bestIndex = getTopN(blacklistSimilarities, 1).firstOrNull()?: continue
                val similarity = blacklistSimilarities[bestIndex]
                if(similarity > bestMatch){
                    bestMatch = similarity
                    unauthorisedPersonName = blackList!![bestIndex].name
                }
            }

            if(bestMatch >= threshold) detectedUnauthorisedPerson = true
        }

        if(!whiteList.isNullOrEmpty() && mode == FaceType.WHITELIST){
            for(faceEmbedding in detectedFacesEmbeddings){
                val whitelistSimilarities = getSimilarities(faceEmbedding, whiteList!!.map{it.embeddings} )
                val bestIndex = getTopN(whitelistSimilarities, 1).firstOrNull()?: continue
                val similarity = whitelistSimilarities[bestIndex]
                if(similarity > bestMatch){
                    bestMatch = similarity
                }
            }
            // This means the detected person is not whitelisted
            // Whitelist is prone to false detection so multiple detections reduce false positives
            if(bestMatch != -1f && bestMatch < threshold){
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
            onDetectedPerson(unauthorisedPersonName, bitmap, mode, bestMatch)
        }
    }

    private suspend fun onDetectedPerson(name: String?, frame: Bitmap, detectionType: FaceType, similarity: Float){
        val currentDetectionTime = System.currentTimeMillis()
        val shouldAlert = currentDetectionTime - lastDetectionTime >= alertFrequency
        if(!shouldAlert) return

        lastDetectionTime = currentDetectionTime

        try {
            detectionLogRepository.insert(
                NewDetectionLog(
                    date = System.currentTimeMillis(),
                    type = detectionType,
                    name = name,
                    similarity = similarity
                )
            )
        }catch (e: Exception){
            Log.e(TAG, "Error inserting detection log: ${e.message}")
        }

        val title = application.getString(R.string.notif_detection_title)
        val messageTitle = if (name != null) "$title: $name" else title

        if(telegramBotToken.isNotEmpty() && telegramChannelId.isNotEmpty()){
            val result = sendTelegramMessage(telegramBotToken, messageTitle, telegramChannelId, frame)
            result.onFailure { error ->
                val errorTitle = application.getString(R.string.notif_alert_error_title)
                val errorContent = application.getString(R.string.notif_alert_error_content)
                showNotification(application, title=errorTitle, channelId = NotificationChannels.ALERTS, text=errorContent)
                Log.e(TAG, "Error sending telegram alert: $error")
            }
        }else{
            val baseContent = application.getString(R.string.notif_detection_content)
            val content = if (name != null) "Detected person: $name. $baseContent" else baseContent
            showNotification(application, title=title, channelId = NotificationChannels.ALERTS, text=content)
        }
    }
}
