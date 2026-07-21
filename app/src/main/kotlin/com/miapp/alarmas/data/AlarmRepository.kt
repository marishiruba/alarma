package com.miapp.alarmas.data

import android.content.Context
import com.miapp.alarmas.alarm.AlarmScheduler
import kotlinx.coroutines.flow.Flow

/**
 * Capa única de acceso a los datos de alarmas. La UI y los ViewModels
 * solo hablan con este repositorio, nunca directamente con el DAO.
 */
class AlarmRepository(context: Context) {

    private val dao = AlarmDatabase.getInstance(context).alarmDao()
    private val scheduler = AlarmScheduler(context)

    fun observeAlarms(): Flow<List<AlarmEntity>> = dao.observeAll()

    suspend fun getAlarm(id: Long): AlarmEntity? = dao.getById(id)

    /** Guarda la alarma (nueva o editada) y (re)programa su disparo si está activada. */
    suspend fun saveAlarm(alarm: AlarmEntity): Long {
        val id = dao.insert(alarm)
        val saved = alarm.copy(id = if (alarm.id == 0L) id else alarm.id)
        if (saved.enabled) {
            val triggerAt = scheduler.schedule(saved)
            dao.update(saved.copy(nextTriggerAt = triggerAt))
        } else {
            scheduler.cancel(saved)
        }
        return saved.id
    }

    suspend fun setEnabled(alarm: AlarmEntity, enabled: Boolean) {
        val updated = alarm.copy(enabled = enabled)
        if (enabled) {
            val triggerAt = scheduler.schedule(updated)
            dao.update(updated.copy(nextTriggerAt = triggerAt))
        } else {
            scheduler.cancel(updated)
            dao.update(updated)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        scheduler.cancel(alarm)
        dao.delete(alarm)
    }

    /** Vuelve a programar todas las alarmas activas. Se usa tras un reinicio del equipo. */
    suspend fun rescheduleAll() {
        dao.getEnabled().forEach { alarm ->
            val triggerAt = scheduler.schedule(alarm)
            dao.update(alarm.copy(nextTriggerAt = triggerAt))
        }
    }

    /** Reprograma una alarma repetitiva luego de que sonó, o la desactiva si era de una sola vez. */
    suspend fun rescheduleAfterRing(alarmId: Long) {
        val alarm = dao.getById(alarmId) ?: return
        if (alarm.daysOfWeek.isEmpty()) {
            scheduler.cancel(alarm)
            dao.update(alarm.copy(enabled = false))
        } else {
            val triggerAt = scheduler.schedule(alarm)
            dao.update(alarm.copy(nextTriggerAt = triggerAt))
        }
    }
}
