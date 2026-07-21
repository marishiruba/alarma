package com.miapp.alarmas.ui.settings

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.miapp.alarmas.data.ThemeMode
import com.miapp.alarmas.ui.alarm.queryFileName
import com.miapp.alarmas.ui.theme.BluePrimary
import com.miapp.alarmas.ui.theme.GreyText
import com.miapp.alarmas.util.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    var testVolume by remember { mutableFloatStateOf(settings.alarmVolume) }

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val name = queryFileName(uri.toString(), context) ?: "Sonido personalizado"
            viewModel.setDefaultSound(uri.toString(), name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
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
            SectionTitle("Sonido")

            Card(
                onClick = { soundPickerLauncher.launch(arrayOf("audio/*")) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = BluePrimary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Cambiar sonido de alarma", style = MaterialTheme.typography.bodyLarge)
                        Text(settings.defaultSoundName, color = GreyText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { playTestSound(context, settings.defaultSoundUri, testVolume) }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Probar sonido", tint = BluePrimary)
                }
                Text("Volumen de prueba", modifier = Modifier.weight(1f))
            }
            Slider(
                value = testVolume,
                onValueChange = { testVolume = it },
                onValueChangeFinished = { viewModel.setAlarmVolume(testVolume) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            SectionTitle("Alarma")

            SwitchRow("Vibración por defecto", settings.defaultVibrate) {
                viewModel.setDefaultVibrate(it)
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Duración del Snooze: ${settings.defaultSnoozeMinutes} min", modifier = Modifier.weight(1f))
            }
            Slider(
                value = settings.defaultSnoozeMinutes.toFloat(),
                onValueChange = { viewModel.setDefaultSnoozeMinutes(it.toInt()) },
                valueRange = 1f..30f,
                steps = 28,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            SectionTitle("Formato de hora")
            RadioRow("Formato 12 horas (a. m. / p. m.)", !settings.use24HourFormat) {
                viewModel.setUse24HourFormat(false)
            }
            RadioRow("Formato 24 horas", settings.use24HourFormat) {
                viewModel.setUse24HourFormat(true)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            SectionTitle("Tema")
            RadioRow("Claro", settings.themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
            RadioRow("Oscuro", settings.themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
            RadioRow("Seguir tema del sistema", settings.themeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            SectionTitle("Permisos y batería")

            Card(
                onClick = { PermissionUtils.requestExactAlarmPermission(context) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Permitir alarmas exactas", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Necesario para que la alarma suene a la hora exacta.",
                        color = GreyText, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                onClick = { PermissionUtils.requestIgnoreBatteryOptimizations(context) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BatteryAlert, contentDescription = null, tint = BluePrimary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Ignorar optimización de batería", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "En Xiaomi (HyperOS) también activa \"Inicio automático\" y \"Sin restricciones\" en Ajustes > Batería > esta app.",
                            color = GreyText, style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Card(
                onClick = { PermissionUtils.openAppSettings(context) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Abrir ajustes de la app", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Para revisar permisos de notificaciones y almacenamiento manualmente.",
                        color = GreyText, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun playTestSound(context: android.content.Context, soundUri: String?, volume: Float) {
    runCatching {
        val uri = soundUri?.let { android.net.Uri.parse(it) }
            ?: android.media.RingtoneManager.getActualDefaultRingtoneUri(context, android.media.RingtoneManager.TYPE_ALARM)
        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(context, uri!!)
            setVolume(volume, volume)
            prepare()
            start()
        }
        player.setOnCompletionListener { it.release() }
        // Se detiene solo tras 3 segundos como prueba corta.
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            runCatching { if (player.isPlaying) player.stop(); player.release() }
        }, 3000)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = BluePrimary, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(title, modifier = Modifier.padding(start = 4.dp))
    }
}
