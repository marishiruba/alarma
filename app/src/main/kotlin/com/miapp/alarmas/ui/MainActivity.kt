package com.miapp.alarmas.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.miapp.alarmas.data.AppSettings
import com.miapp.alarmas.data.SettingsRepository
import com.miapp.alarmas.ui.navigation.AppNavGraph
import com.miapp.alarmas.ui.theme.AlarmAppTheme
import com.miapp.alarmas.util.PermissionUtils

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RequestRuntimePermissions()

            val settingsRepository = remember(this) { SettingsRepository(applicationContext) }
            val settings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())

            AlarmAppTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
                    AppNavGraph()
                }
            }
        }
    }
}

/** Solicita en tiempo de ejecución los permisos necesarios (notificaciones y alarmas exactas). */
@Composable
private fun RequestRuntimePermissions() {
    val context = LocalContext.current

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* resultado manejado implícitamente por el sistema */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (!PermissionUtils.canScheduleExactAlarms(context)) {
            PermissionUtils.requestExactAlarmPermission(context)
        }
    }
}
