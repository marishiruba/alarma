package com.miapp.alarmas.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.miapp.alarmas.data.AlarmEntity
import com.miapp.alarmas.util.TimeUtils

const val EXTRA_ALARM_ID = "extra_alarm_id"
const val EXTRA_ALARM_LABEL = "extra_alarm_label"
const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"

/**
 * Encapsula toda la interacción con [AlarmManager]. Usa setExactAndAllowWhileIdle
 * para garantizar que la alarma suene incluso en Doze/reposo profundo.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Programa la alarma y devuelve el epoch millis en que sonará. */
    fun schedule(alarm: AlarmEntity): Long {
        val triggerAt = TimeUtils.nextTriggerMillis(alarm)
        val pendingIntent = buildPendingIntent(alarm)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // El usuario revocó el permiso de alarmas exactas: no se puede programar.
            // La UI debe pedir el permiso mediante PermissionUtils antes de llegar aquí.
        }
        return triggerAt
    }

    fun cancel(alarm: AlarmEntity) {
        alarmManager.cancel(buildPendingIntent(alarm))
    }

    /** Reprograma una alarma ya sonando para dentro de [minutes] (usado por posponer/snooze). */
    fun scheduleSnooze(alarm: AlarmEntity, minutes: Int): Long {
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        val pendingIntent = buildPendingIntent(alarm)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        return triggerAt
    }

    private fun buildPendingIntent(alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(EXTRA_ALARM_MINUTE, alarm.minute)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
