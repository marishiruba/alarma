package com.miapp.alarmas.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.miapp.alarmas.R
import com.miapp.alarmas.ui.ringing.RingingActivity

const val CHANNEL_ID = "alarm_channel"
const val ACTION_STOP = "com.miapp.alarmas.action.STOP"
const val ACTION_SNOOZE = "com.miapp.alarmas.action.SNOOZE"
const val NOTIFICATION_ID = 1001

object NotificationHelper {

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Alarmas",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notificaciones de alarmas sonando"
                    setBypassDnd(true)
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /** Notificación de pantalla completa: en teléfono desbloqueado abre [RingingActivity] directo. */
    fun buildRingingNotification(
        context: Context,
        alarmId: Long,
        label: String
    ): android.app.Notification {
        ensureChannel(context)

        val fullScreenIntent = Intent(context, RingingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, alarmId.toInt(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = ACTION_STOP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, alarmId.toInt() + 100_000, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, alarmId.toInt() + 200_000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(label.ifBlank { "Alarma" })
            .setContentText("Toca para abrir la alarma")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, "Detener", stopPendingIntent)
            .addAction(0, "Posponer", snoozePendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()
    }
}
