package com.fpf.sentinellens.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fpf.sentinellens.MainActivity
import com.fpf.sentinellens.lib.camera.ImageCaptureHelper
import com.fpf.sentinellens.lib.camera.ImageCaptureListener

class CameraForegroundService : Service(), ImageCaptureListener {

    companion object {
        private const val TAG = "CameraForegroundService"
        private const val CHANNEL_ID = "CameraServiceChannel"
        private const val NOTIFICATION_ID = 101
    }

    private lateinit var imageCaptureHelper: ImageCaptureHelper

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceNotification()
        imageCaptureHelper = ImageCaptureHelper(this, this)
    }

    private fun startForegroundServiceNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Taking Photo")
            .setContentText("Camera is active in foreground")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Camera Foreground", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        imageCaptureHelper.startImageCapture()
        // Return not sticky so that the service doesn't restart automatically.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // The helper cleans up resources when the capture is done.
        super.onDestroy()
    }

    // --- ImageCaptureListener Callbacks ---

    override fun onImageCaptured(imageUri: android.net.Uri) {
        Log.d(TAG, "Image captured and saved at: $imageUri")
        // Optionally, perform additional operations with the image.
        stopSelf()
    }

    override fun onError(exception: Exception) {
        Log.e(TAG, "Error during image capture", exception)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
