package com.miapp.alarmas.data

import androidx.room.TypeConverter

/** Convierte el conjunto de días de la semana a/desde texto para poder guardarlo en Room. */
class Converters {

    @TypeConverter
    fun fromDaySet(days: Set<Int>): String = days.sorted().joinToString(",")

    @TypeConverter
    fun toDaySet(raw: String): Set<Int> =
        if (raw.isBlank()) emptySet()
        else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
}
