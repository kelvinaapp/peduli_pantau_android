package com.example.pedulipantau.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.pedulipantau.MainActivity
import com.example.pedulipantau.R

class NotificationHelper(context: Context, notificationId : Int) {

    private val TAG = "NotificationHelperLog"

    var notificationId : Int
    var context : Context
    lateinit var notificationManager : NotificationManager

    init {
        this.notificationId = notificationId
        this.context = context
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int = NotificationManager.IMPORTANCE_NONE,
        isLightOn: Boolean = false,
        lightColor: Int = Color.BLUE,
        isVibrate: Boolean = false,
    ): String {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        )
        channel.enableLights(isLightOn)
        channel.lightColor = lightColor
        channel.enableVibration(isVibrate)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)

        return channelId
    }

    fun buildNotification(channelId: String, contentTitle : String, contentText: String): NotificationCompat.Builder {
        lateinit var builder : NotificationCompat.Builder
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        if (notificationId >= 2000) {
            Log.d(TAG, "buildNotification: warn builder")
            var color = context.resources.getColor(R.color.warning)
            if(notificationId == 3000)
                color = context.resources.getColor(R.color.danger)

            builder = NotificationCompat.Builder(context, channelId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_caution)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(color)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
        } else if(notificationId == 1000) {
            Log.d(TAG, "buildNotification: normal builder")
            builder = NotificationCompat.Builder(context, channelId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_bluetooth)
                .setContentIntent(pendingIntent)
                .setTicker("Application is Active")
        }

        return builder
    }

    fun showNotification(builder : NotificationCompat.Builder) {
        Log.d(TAG, "showNotification: $notificationId")
        notificationManager?.notify(notificationId, builder.build())
    }

    fun showWarningNotification(title: String, content: String) {
        var tempChannelId = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tempChannelId = createNotificationChannel(
                NotificationConstant.WARNING_CHANNEL_ID,
                NotificationConstant.WARNING_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH,
                true,
                Color.YELLOW,
                true
            )
        }
        var builder = buildNotification(tempChannelId, title, content)
        showNotification(builder)
    }

    fun showDangerNotification(title: String, content: String) {
        var tempChannelId = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tempChannelId = createNotificationChannel(
                NotificationConstant.DANGER_CHANNEL_ID,
                NotificationConstant.DANGER_CHANNEL_ID,
                NotificationManager.IMPORTANCE_HIGH,
                true,
                Color.RED,
                true
            )
        }
        var builder = buildNotification(tempChannelId, title, content)
        showNotification(builder)
    }

    fun buildServiceNotification(title: String, content: String) : NotificationCompat.Builder {
        var tempChannelId = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tempChannelId = createNotificationChannel(
                NotificationConstant.DEFAULT_CHANNEL_ID,
                NotificationConstant.DEFAULT_CHANNEL_ID,
            )
        }

        return buildNotification(tempChannelId, title, content)
    }

}