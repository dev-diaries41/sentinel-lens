package com.fpf.sentinellens.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.Surface
import androidx.core.app.NotificationCompat
import com.fpf.sentinellens.MainActivity
import com.fpf.sentinellens.R
import com.fpf.sentinellens.lib.Storage
import com.fpf.sentinellens.lib.camera.IMlVideoCaptureListener
import com.fpf.sentinellens.lib.camera.VideoCaptureHelper
import com.fpf.sentinellens.lib.camera.VideoCaptureListener
import com.fpf.sentinellens.ui.screens.settings.AppSettings
import kotlinx.serialization.json.Json

class SurveillanceForegroundService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 102
        var previewSurface: Surface? = null

        fun updatePreviewSurface(surface: Surface) {
            previewSurface = surface
        }
    }

    private lateinit var videoCaptureHelper: VideoCaptureHelper
    private var videoCaptureListener: IMlVideoCaptureListener? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()

        val storage = Storage.getInstance(application)
        val jsonSettings = storage.getItem("app_settings")
        val appSettings = if (jsonSettings != null) {
            try {
                Json.decodeFromString<AppSettings>(jsonSettings)
            } catch (e: Exception) {
                Log.e("Settings", "Failed to decode settings", e)
                AppSettings()
            }
        } else {
            AppSettings()
        }

        videoCaptureListener = VideoCaptureListener(
            application,
            threshold = appSettings.similarityThreshold,
            alertFrequency = appSettings.alertFrequency,
            telegramBotToken = appSettings.telegramBotToken,
            telegramChannelId = appSettings.telegramChannelId,
            mode = appSettings.mode
        )
        videoCaptureHelper = VideoCaptureHelper(this,
            listener = videoCaptureListener,
            isFrameProcessingActive = true,
            frameInterval = appSettings.frameInterval,
            maxDuration = appSettings.maxDuration,
            lensFacing = appSettings.cameraType
        )
    }

    private fun startForegroundServiceNotification() {
        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.surveillance_foreground_channel_id))
            .setContentTitle(getString(R.string.notif_surveillance_foreground_title))
            .setContentText(getString(R.string.notif_surveillance_foreground_content))
            .setSmallIcon(R.drawable.icon)
            .setContentIntent(activityPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(R.string.surveillance_foreground_channel_id),
            getString(R.string.surveillance_foreground_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        previewSurface?.let{ videoCaptureHelper.setPreviewSurface(surface = it) }
        videoCaptureHelper.startCameraAndRecording()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        videoCaptureHelper.stopVideoCapture()
        videoCaptureListener?.closeSession()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}