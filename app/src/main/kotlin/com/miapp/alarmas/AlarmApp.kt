package com.miapp.alarmas

import android.app.Application
import com.miapp.alarmas.alarm.NotificationHelper

class AlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
