package com.miapp.alarmas.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miapp.alarmas.data.AppSettings
import com.miapp.alarmas.data.SettingsRepository
import com.miapp.alarmas.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val settings = repository.settingsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings()
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.setThemeMode(mode) }
    fun setUse24HourFormat(value: Boolean) = viewModelScope.launch { repository.setUse24HourFormat(value) }
    fun setDefaultVibrate(value: Boolean) = viewModelScope.launch { repository.setDefaultVibrate(value) }
    fun setDefaultSnoozeMinutes(value: Int) = viewModelScope.launch { repository.setDefaultSnoozeMinutes(value) }
    fun setDefaultSound(uri: String, name: String) = viewModelScope.launch { repository.setDefaultSound(uri, name) }
    fun setAlarmVolume(value: Float) = viewModelScope.launch { repository.setAlarmVolume(value) }
}
