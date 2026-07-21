package com.miapp.alarmas.ui.ringing

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miapp.alarmas.alarm.AlarmRingingService
import com.miapp.alarmas.alarm.EXTRA_ALARM_LABEL
import com.miapp.alarmas.ui.theme.AlarmAppTheme
import com.miapp.alarmas.util.TimeUtils
import java.util.Calendar

class RingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowOnLockScreen()

        val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "Alarma"
        val now = Calendar.getInstance()
        val (time, suffix) = TimeUtils.formatHourMinute(
            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false
        )

        setContent {
            AlarmAppTheme {
                RingingScreen(
                    time = time,
                    suffix = suffix,
                    label = label,
                    onStop = {
                        AlarmRingingService.instance?.stopRinging()
                        finish()
                    },
                    onSnooze = {
                        AlarmRingingService.instance?.snooze(10)
                        finish()
                    }
                )
            }
        }
    }

    /** Asegura que la pantalla se encienda y se muestre sobre el bloqueo, incluso bloqueado con PIN. */
    private fun setShowOnLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}

@Composable
private fun RingingScreen(
    time: String,
    suffix: String,
    label: String,
    onStop: () -> Unit,
    onSnooze: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "$time $suffix",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 64.sp)
            )

            Spacer(Modifier.height(64.dp))

            Button(
                onClick = onStop,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text("DETENER", color = Color.White, modifier = Modifier.padding(vertical = 12.dp))
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSnooze,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                Text("POSPONER", modifier = Modifier.padding(vertical = 12.dp))
            }
        }
    }
}
