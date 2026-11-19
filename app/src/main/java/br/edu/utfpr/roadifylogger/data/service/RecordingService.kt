
package br.edu.utfpr.roadifylogger.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import br.edu.utfpr.roadifylogger.MainActivity
import br.edu.utfpr.roadifylogger.R

class RecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "RoadifyRecordingChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        val action = intent?.action

        when (action) {
            ACTION_START -> {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification()
                )
            }

            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                // Fallback caso o serviço seja reiniciado.
                startForeground(
                    NOTIFICATION_ID,
                    createNotification()
                )
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val notificationIntent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or
                    PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(
                getString(R.string.recording_notification_text)
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.recording_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(
                    R.string.recording_notification_description
                )
            }

            val manager =
                getSystemService(NotificationManager::class.java)

            manager?.createNotificationChannel(channel)
        }
    }
}
