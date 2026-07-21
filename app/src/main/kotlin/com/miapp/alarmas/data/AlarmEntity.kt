package com.miapp.alarmas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa una alarma guardada en la base de datos.
 * [daysOfWeek] usa el estándar de Calendar: 1=domingo ... 7=sábado.
 * Si está vacío, la alarma es de "una sola vez".
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val enabled: Boolean = true,
    val daysOfWeek: Set<Int> = emptySet(),
    val vibrate: Boolean = true,
    val soundEnabled: Boolean = true,
    val soundUri: String? = null,
    val soundName: String = "Sonido predeterminado",
    val snoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    // Marca de tiempo (epoch millis) del próximo disparo programado, usado para
    // mostrar "Suena en Xh Ym" y para restaurar tras un reinicio.
    val nextTriggerAt: Long = 0L
)
