package com.miapp.alarmas.alarm

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationManagerCompat
import com.miapp.alarmas.data.AlarmDatabase
import com.miapp.alarmas.data.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio en primer plano: es el único momento en que la app mantiene un
 * proceso activo de larga duración, y solo mientras la alarma está sonando.
 */
class AlarmRingingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var currentAlarmId: Long = -1L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "Alarma"
        currentAlarmId = alarmId

        val notification = NotificationHelper.buildRingingNotification(this, alarmId, label)
        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            val dao = AlarmDatabase.getInstance(applicationContext).alarmDao()
            val alarm = dao.getById(alarmId)
            startRinging(
                soundUri = alarm?.soundUri,
                soundEnabled = alarm?.soundEnabled ?: true,
                vibrate = alarm?.vibrate ?: true
            )
        }

        return START_STICKY
    }

    private fun startRinging(soundUri: String?, soundEnabled: Boolean, vibrate: Boolean) {
        if (soundEnabled) {
            try {
                val uri = soundUri?.let { Uri.parse(it) }
                    ?: RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(this@AlarmRingingService, uri)
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (_: Exception) {
                // Si el archivo fue movido/borrado, se omite el sonido en vez de crashear.
            }
        }

        if (vibrate) {
            val pattern = longArrayOf(0, 800, 500)
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(VibratorManager::class.java)
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }

    /** Detiene el sonido/vibración y finaliza el servicio. Se usa desde DETENER. */
    fun stopRinging() {
        mediaPlayer?.let { runCatching { it.stop(); it.release() } }
        mediaPlayer = null
        vibrator?.cancel()
        scope.launch {
            AlarmRepository(applicationContext).rescheduleAfterRing(currentAlarmId)
        }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /** Reprograma para dentro de X minutos y detiene el sonido actual. */
    fun snooze(minutes: Int) {
        mediaPlayer?.let { runCatching { it.stop(); it.release() } }
        mediaPlayer = null
        vibrator?.cancel()
        scope.launch {
            val dao = AlarmDatabase.getInstance(applicationContext).alarmDao()
            val alarm = dao.getById(currentAlarmId)
            if (alarm != null) {
                AlarmScheduler(applicationContext).scheduleSnooze(alarm, minutes)
            }
        }
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        mediaPlayer?.let { runCatching { it.release() } }
        vibrator?.cancel()
        if (instance == this) instance = null
        super.onDestroy()
    }

    companion object {
        /** Referencia viva del servicio actual para que la Activity/Receiver puedan detenerlo. */
        var instance: AlarmRingingService? = null
    }
}
