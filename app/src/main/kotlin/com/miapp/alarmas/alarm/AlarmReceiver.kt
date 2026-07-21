package com.miapp.alarmas.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Se dispara exactamente a la hora programada, incluso con pantalla apagada,
 * teléfono bloqueado o la app cerrada/eliminada de recientes.
 * Su única responsabilidad es arrancar el Foreground Service que hace sonar la alarma.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, intent.getStringExtra(EXTRA_ALARM_LABEL))
            putExtra(EXTRA_ALARM_HOUR, intent.getIntExtra(EXTRA_ALARM_HOUR, 0))
            putExtra(EXTRA_ALARM_MINUTE, intent.getIntExtra(EXTRA_ALARM_MINUTE, 0))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
