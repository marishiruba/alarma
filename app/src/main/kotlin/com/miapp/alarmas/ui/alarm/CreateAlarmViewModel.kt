package com.miapp.alarmas.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miapp.alarmas.data.AlarmEntity
import com.miapp.alarmas.data.AlarmRepository
import com.miapp.alarmas.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateAlarmUiState(
    val id: Long = 0L,
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "",
    val daysOfWeek: Set<Int> = emptySet(),
    val vibrate: Boolean = true,
    val soundEnabled: Boolean = true,
    val soundUri: String? = null,
    val soundName: String = "Sonido predeterminado",
    val snoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val use24HourFormat: Boolean = false,
    val isEditing: Boolean = false,
    val saved: Boolean = false
)

class CreateAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(CreateAlarmUiState())
    val uiState: StateFlow<CreateAlarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    use24HourFormat = settings.use24HourFormat,
                    vibrate = if (_uiState.value.isEditing) _uiState.value.vibrate else settings.defaultVibrate,
                    snoozeMinutes = if (_uiState.value.isEditing) _uiState.value.snoozeMinutes else settings.defaultSnoozeMinutes,
                    soundUri = _uiState.value.soundUri ?: settings.defaultSoundUri,
                    soundName = if (_uiState.value.soundUri != null) _uiState.value.soundName else settings.defaultSoundName
                )
            }
        }
    }

    fun loadAlarm(id: Long) {
        if (id <= 0) return
        viewModelScope.launch {
            repository.getAlarm(id)?.let { alarm ->
                _uiState.value = _uiState.value.copy(
                    id = alarm.id,
                    hour = alarm.hour,
                    minute = alarm.minute,
                    label = alarm.label,
                    daysOfWeek = alarm.daysOfWeek,
                    vibrate = alarm.vibrate,
                    soundEnabled = alarm.soundEnabled,
                    soundUri = alarm.soundUri,
                    soundName = alarm.soundName,
                    snoozeEnabled = alarm.snoozeEnabled,
                    snoozeMinutes = alarm.snoozeMinutes,
                    isEditing = true
                )
            }
        }
    }

    fun setTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(hour = hour, minute = minute)
    }

    fun setLabel(label: String) {
        _uiState.value = _uiState.value.copy(label = label)
    }

    fun toggleDay(day: Int) {
        val current = _uiState.value.daysOfWeek
        _uiState.value = _uiState.value.copy(
            daysOfWeek = if (day in current) current - day else current + day
        )
    }

    fun setVibrate(value: Boolean) {
        _uiState.value = _uiState.value.copy(vibrate = value)
    }

    fun setSoundEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(soundEnabled = value)
    }

    fun setSound(uri: String, name: String) {
        _uiState.value = _uiState.value.copy(soundUri = uri, soundName = name)
    }

    fun setSnoozeEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(snoozeEnabled = value)
    }

    fun setSnoozeMinutes(minutes: Int) {
        _uiState.value = _uiState.value.copy(snoozeMinutes = minutes)
    }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            repository.saveAlarm(
                AlarmEntity(
                    id = s.id,
                    hour = s.hour,
                    minute = s.minute,
                    label = s.label,
                    enabled = true,
                    daysOfWeek = s.daysOfWeek,
                    vibrate = s.vibrate,
                    soundEnabled = s.soundEnabled,
                    soundUri = s.soundUri,
                    soundName = s.soundName,
                    snoozeEnabled = s.snoozeEnabled,
                    snoozeMinutes = s.snoozeMinutes
                )
            )
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }
}
