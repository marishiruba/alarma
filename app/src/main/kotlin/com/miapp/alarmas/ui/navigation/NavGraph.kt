package com.miapp.alarmas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.miapp.alarmas.ui.alarm.AlarmListScreen
import com.miapp.alarmas.ui.alarm.AlarmListViewModel
import com.miapp.alarmas.ui.alarm.CreateAlarmScreen
import com.miapp.alarmas.ui.alarm.CreateAlarmViewModel
import com.miapp.alarmas.ui.settings.SettingsScreen
import com.miapp.alarmas.ui.settings.SettingsViewModel

private object Routes {
    const val LIST = "list"
    const val CREATE = "create?alarmId={alarmId}"
    const val SETTINGS = "settings"
    fun createRoute(alarmId: Long? = null) = "create?alarmId=${alarmId ?: -1L}"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            val viewModel: AlarmListViewModel = viewModel()
            AlarmListScreen(
                viewModel = viewModel,
                onAddAlarm = { navController.navigate(Routes.createRoute()) },
                onEditAlarm = { id -> navController.navigate(Routes.createRoute(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.CREATE,
            arguments = listOf(navArgument("alarmId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            val viewModel: CreateAlarmViewModel = viewModel()
            CreateAlarmScreen(
                viewModel = viewModel,
                alarmId = if (alarmId > 0) alarmId else null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
