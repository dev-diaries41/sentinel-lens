package com.fpf.sentinellens.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.view.Surface
import androidx.core.app.NotificationCompat
import com.fpf.sentinellens.MainActivity
import com.fpf.sentinellens.R
import com.fpf.sentinellens.data.faces.FacesRepository
import com.fpf.sentinellens.data.logs.LogRepository
import com.fpf.sentinellens.notifications.NotificationChannels
import com.fpf.sentinellens.surveillance.IVideoCaptureListener
import com.fpf.sentinellens.surveillance.VideoCaptureHelper
import com.fpf.sentinellens.surveillance.VideoCaptureListener
import com.fpf.sentinellens.settings.loadSettings
import com.fpf.sentinellens.surveillance.FaceType
import com.fpf.smartscansdk.ml.detectors.FaceDetector
import com.fpf.smartscansdk.ml.embeddings.inception.InceptionResnetFaceEmbedder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SurveillanceForegroundService : Service(), KoinComponent {
    companion object {
        private const val NOTIFICATION_ID = 102
        const val MODE = "mode"

        var previewSurface: Surface? = null

        fun updatePreviewSurface(surface: Surface) {
            previewSurface = surface
        }
    }

    private lateinit var videoCaptureHelper: VideoCaptureHelper
    private var videoCaptureListener: IVideoCaptureListener? = null

    private val facesRepository: FacesRepository by inject()
    private val detectionLogRepository: LogRepository by inject()
    private val faceComparer: InceptionResnetFaceEmbedder by inject()
    private val faceDetector: FaceDetector by inject()
    private val sharedPrefs: SharedPreferences by inject()



    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NotificationChannels.SURVEILLANCE_SERVICE)
            .setContentTitle(getString(R.string.notif_surveillance_foreground_title))
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(activityPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mode = FaceType.valueOf(intent?.getStringExtra(MODE)?: error("Invalid mode"))
        setupVideoCapturer(mode)
        previewSurface?.let{ videoCaptureHelper.setPreviewSurface(surface = it) }
        videoCaptureHelper.startCameraAndRecording()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        videoCaptureHelper.stopVideoCapture()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun setupVideoCapturer(mode: FaceType){
        val settings = loadSettings(sharedPrefs)

        videoCaptureListener = VideoCaptureListener(
            application,
            threshold = settings.similarityThreshold,
            alertFrequency = settings.alertFrequency,
            telegramBotToken = settings.telegramBotToken,
            telegramChannelId = settings.telegramChannelId,
            mode = mode,
            faceDetector = faceDetector,
            faceComparer = faceComparer,
            faceRepository = facesRepository,
            detectionLogRepository = detectionLogRepository
        )

        videoCaptureHelper = VideoCaptureHelper(
            context= applicationContext,
            listener = videoCaptureListener,
            isFrameProcessingActive = true,
            frameInterval = settings.frameInterval,
            maxDuration = settings.maxDuration,
            lensFacing = settings.cameraType
        )
    }
}