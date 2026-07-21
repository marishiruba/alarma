package com.miapp.alarmas.ui.alarm

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miapp.alarmas.ui.theme.BluePrimary
import com.miapp.alarmas.ui.theme.GreyText
import java.util.Calendar

private val dayLabels = listOf(
    Calendar.SUNDAY to "D", Calendar.MONDAY to "L", Calendar.TUESDAY to "M",
    Calendar.WEDNESDAY to "M", Calendar.THURSDAY to "J", Calendar.FRIDAY to "V",
    Calendar.SATURDAY to "S"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlarmScreen(
    viewModel: CreateAlarmViewModel,
    alarmId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(alarmId) {
        if (alarmId != null && alarmId > 0) viewModel.loadAlarm(alarmId)
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    val timePickerState = rememberTimePickerState(
        initialHour = state.hour,
        initialMinute = state.minute,
        is24Hour = state.use24HourFormat
    )
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        viewModel.setTime(timePickerState.hour, timePickerState.minute)
    }

    // Selector de archivos del sistema (Storage Access Framework).
    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val name = queryFileName(uri.toString(), context) ?: "Sonido personalizado"
            viewModel.setSound(uri.toString(), name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Editar alarma" else "Nueva alarma") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Guardar", tint = BluePrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            TimeInput(state = timePickerState, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(24.dp))
            Text("Repetir", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayLabels.forEach { (day, label) ->
                    FilterChip(
                        selected = day in state.daysOfWeek,
                        onClick = { viewModel.toggleDay(day) },
                        label = { Text(label) },
                        shape = CircleShape
                    )
                }
            }
            Text(
                if (state.daysOfWeek.isEmpty()) "Se activará una sola vez" else "Se repite semanalmente",
                style = MaterialTheme.typography.bodyMedium,
                color = GreyText,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = state.label,
                onValueChange = { viewModel.setLabel(it) },
                label = { Text("Nombre de la alarma") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            SettingRow(
                title = "Vibración",
                checked = state.vibrate,
                onCheckedChange = { viewModel.setVibrate(it) }
            )

            SettingRow(
                title = "Sonido",
                checked = state.soundEnabled,
                onCheckedChange = { viewModel.setSoundEnabled(it) }
            )

            if (state.soundEnabled) {
                Card(
                    onClick = { soundPickerLauncher.launch(arrayOf("audio/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.MusicNote, contentDescription = null, tint = BluePrimary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Sonido de la alarma", style = MaterialTheme.typography.bodyMedium)
                            Text(state.soundName, color = GreyText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SettingRow(
                title = "Posponer (Snooze)",
                checked = state.snoozeEnabled,
                onCheckedChange = { viewModel.setSnoozeEnabled(it) }
            )
            if (state.snoozeEnabled) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Duración: ${state.snoozeMinutes} min", modifier = Modifier.weight(1f))
                    TextButton(onClick = {
                        if (state.snoozeMinutes > 5) viewModel.setSnoozeMinutes(state.snoozeMinutes - 5)
                    }) { Text("-") }
                    TextButton(onClick = {
                        if (state.snoozeMinutes < 30) viewModel.setSnoozeMinutes(state.snoozeMinutes + 5)
                    }) { Text("+") }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Obtiene el nombre visible de un archivo a partir de su URI (content resolver). */
fun queryFileName(uriString: String, context: android.content.Context): String? {
    val uri = android.net.Uri.parse(uriString)
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return null
}
