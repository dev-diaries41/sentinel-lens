package com.fpf.sentinellens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fpf.sentinellens.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel(
            channelId = getString(R.string.sentinellens_general_channel_id),
            channelName = getString(R.string.sentinellens_general_channel_name),
        )

    setContent {
            MyAppTheme {
                MainScreen()
            }
        }
    }
    private fun createNotificationChannel(channelId: String, channelName: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }
}
