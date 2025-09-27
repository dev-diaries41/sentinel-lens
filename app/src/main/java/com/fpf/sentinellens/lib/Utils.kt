package com.fpf.sentinellens.lib

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fpf.sentinellens.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun toDateString(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getTimeInMinutesAndSeconds(milliseconds: Long): Pair<Long, Long> {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    return Pair(minutes, seconds % 60)
}
fun showNotification(context: Context, title: String, text: String, id: Int = 1001) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }
    val channelId = context.getString(R.string.sentinellens_general_channel_id)
    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.icon)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        notify(id, notificationBuilder.build())
    }
}
