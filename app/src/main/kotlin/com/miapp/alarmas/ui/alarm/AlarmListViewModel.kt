package com.miapp.alarmas.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miapp.alarmas.data.AlarmEntity
import com.miapp.alarmas.data.AlarmRepository
import com.miapp.alarmas.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlarmListUiState(
    val alarms: List<AlarmEntity> = emptyList(),
    val use24HourFormat: Boolean = false
)

class AlarmListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)
    private val settingsRepository = SettingsRepository(application)

    val uiState = combine(
        repository.observeAlarms(),
        settingsRepository.settingsFlow
    ) { alarms, settings ->
        AlarmListUiState(alarms = alarms, use24HourFormat = settings.use24HourFormat)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AlarmListUiState()
    )

    fun toggleAlarm(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(alarm, enabled) }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch { repository.deleteAlarm(alarm) }
    }
}
