package com.miapp.alarmas.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.miapp.alarmas.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Al reiniciar el teléfono, AlarmManager pierde todas las alarmas programadas.
 * Este receiver las restaura leyendo la base de datos.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val pendingResult = goAsync()
        val repository = AlarmRepository(context.applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
