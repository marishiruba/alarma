package com.miapp.alarmas.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Recibe las acciones de los botones de la notificación (Detener / Posponer). */
class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP -> AlarmRingingService.instance?.stopRinging()
            ACTION_SNOOZE -> AlarmRingingService.instance?.snooze(10)
        }
    }
}
