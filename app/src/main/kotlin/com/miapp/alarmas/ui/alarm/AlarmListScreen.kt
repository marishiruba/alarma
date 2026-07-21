package com.miapp.alarmas.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miapp.alarmas.data.AlarmEntity
import com.miapp.alarmas.ui.theme.BlackText
import com.miapp.alarmas.ui.theme.BlueLightCard
import com.miapp.alarmas.ui.theme.BluePrimary
import com.miapp.alarmas.ui.theme.GreyText
import com.miapp.alarmas.ui.theme.TrackOff
import com.miapp.alarmas.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    viewModel: AlarmListViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Alarma rápida",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm,
                containerColor = BluePrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar alarma", tint = Color.White)
            }
        },
        bottomBar = { AlarmBottomBar() }
    ) { padding ->
        if (state.alarms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay alarmas. Toca + para crear una.", color = GreyText)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(state.alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        use24Hour = state.use24HourFormat,
                        onToggle = { checked -> viewModel.toggleAlarm(alarm, checked) },
                        onClick = { onEditAlarm(alarm.id) },
                        onDelete = { viewModel.deleteAlarm(alarm) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    use24Hour: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val (time, suffix) = TimeUtils.formatHourMinute(alarm.hour, alarm.minute, use24Hour)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.enabled) BlueLightCard else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    if (alarm.label.isNotBlank()) {
                        Text(
                            alarm.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = GreyText
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            time,
                            style = MaterialTheme.typography.headlineLarge,
                            color = BlackText
                        )
                        Spacer(Modifier.width(8.dp))
                        if (suffix.isNotEmpty()) {
                            Text(
                                suffix,
                                style = MaterialTheme.typography.bodyLarge,
                                color = BlackText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = { menuExpanded = false; onClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        TimeUtils.daysText(alarm.daysOfWeek),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreyText
                    )
                    if (alarm.enabled && alarm.nextTriggerAt > 0) {
                        Text(
                            TimeUtils.remainingText(alarm.nextTriggerAt),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreyText
                        )
                    }
                }
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BluePrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = TrackOff
                    )
                )
            }
        }
    }
}

@Composable
private fun AlarmBottomBar() {
    var selectedIndex by remember { mutableStateOf(0) }
    val icons = listOf(
        Icons.Filled.Alarm,
        Icons.Filled.Bedtime,
        Icons.Filled.Notifications,
        Icons.Filled.HourglassEmpty,
        Icons.Filled.Timer
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        icons.forEachIndexed { index, icon ->
            BottomBarItem(
                icon = icon,
                selected = selectedIndex == index,
                onClick = { selectedIndex = index }
            )
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) BluePrimary else GreyText,
            modifier = Modifier.size(26.dp)
        )
        if (selected) {
            Box(
                Modifier
                    .padding(top = 4.dp)
                    .size(width = 18.dp, height = 3.dp)
                    .background(BluePrimary, RoundedCornerShape(50))
            )
        }
    }
}
