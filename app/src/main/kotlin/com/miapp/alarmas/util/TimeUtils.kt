package com.miapp.alarmas.util

import com.miapp.alarmas.data.AlarmEntity
import java.util.Calendar
import java.util.Locale

object TimeUtils {

    /**
     * Calcula el próximo epoch millis en que esta alarma debe sonar.
     * Si [AlarmEntity.daysOfWeek] está vacío, es "una sola vez": la próxima
     * ocurrencia de esa hora (hoy si aún no pasó, si no mañana).
     */
    fun nextTriggerMillis(alarm: AlarmEntity, now: Calendar = Calendar.getInstance()): Long {
        val candidate = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (alarm.daysOfWeek.isEmpty()) {
            if (candidate.timeInMillis <= now.timeInMillis) {
                candidate.add(Calendar.DAY_OF_YEAR, 1)
            }
            return candidate.timeInMillis
        }

        // Busca el próximo día (entre hoy y los siguientes 7 días) que esté en daysOfWeek.
        for (i in 0..7) {
            val check = Calendar.getInstance().apply {
                timeInMillis = candidate.timeInMillis
                add(Calendar.DAY_OF_YEAR, i)
            }
            val dow = check.get(Calendar.DAY_OF_WEEK)
            val isToday = i == 0
            if (dow in alarm.daysOfWeek && (!isToday || check.timeInMillis > now.timeInMillis)) {
                return check.timeInMillis
            }
        }
        return candidate.timeInMillis
    }

    /** Texto tipo "Suena en 2 h 43 m" o "Suena en 45 m". */
    fun remainingText(triggerAt: Long, now: Long = System.currentTimeMillis()): String {
        val diff = (triggerAt - now).coerceAtLeast(0L)
        val totalMinutes = diff / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "Suena en ${hours} h ${minutes} m" else "Suena en ${minutes} m"
    }

    /** Formatea hora:minuto según formato 12/24, devuelve Pair(horaTexto, sufijoAmPm o vacío). */
    fun formatHourMinute(hour: Int, minute: Int, use24h: Boolean): Pair<String, String> {
        return if (use24h) {
            String.format(Locale.getDefault(), "%02d:%02d", hour, minute) to ""
        } else {
            val h12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val suffix = if (hour < 12) "a. m." else "p. m."
            String.format(Locale.getDefault(), "%02d:%02d", h12, minute) to suffix
        }
    }

    private val DAY_NAMES = mapOf(
        Calendar.SUNDAY to "dom", Calendar.MONDAY to "lun", Calendar.TUESDAY to "mar",
        Calendar.WEDNESDAY to "mié", Calendar.THURSDAY to "jue", Calendar.FRIDAY to "vie",
        Calendar.SATURDAY to "sáb"
    )

    /** Convierte el set de días en "lun mar mié" ordenado empezando el domingo. */
    fun daysText(days: Set<Int>): String {
        if (days.isEmpty()) return "Una vez"
        val order = listOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )
        return order.filter { it in days }.joinToString(" ") { DAY_NAMES[it] ?: "" }
    }
}
