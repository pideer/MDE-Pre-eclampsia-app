package io.github.pideer.pes

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.pideer.pes.ble.repo.BatteryRepository
import io.github.pideer.pes.ble.repo.ConfigRepository
import io.github.pideer.pes.ble.repo.DeviceInfoRepository
import io.github.pideer.pes.ui.components.Banner
import io.github.pideer.pes.ui.components.DeviceInfoBanner
import io.github.pideer.pes.ui.screens.ConfigureScreen
import io.github.pideer.pes.ui.screens.ConnectScreen
import io.github.pideer.pes.ui.screens.ConsoleScreen
import io.github.pideer.pes.ui.screens.DemoScreen
import io.github.pideer.pes.ui.screens.MenuScreen
import io.github.pideer.pes.ui.screens.NewPatientScreen
import io.github.pideer.pes.ui.screens.ViewDataScreen
import io.github.pideer.pes.ui.state.NavEvent
import io.github.pideer.pes.ui.viewmodels.ScanViewModel

enum class AppScreen {
    DeviceConnection, Options, ViewData, Configure, NewPatient, Console, Demo, Calibrate;

    override fun toString(): String = when (this) {
        DeviceConnection -> "Device Connection"
        ViewData -> "View Data"
        Configure -> "Configure"
        NewPatient -> "New Patient"
        Console -> "Console"
        Demo -> "Demo"
        else -> this.toString()
    }
}

@Composable
fun PESApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isConnectionScreen = currentRoute == AppScreen.DeviceConnection.name
    val scanVM: ScanViewModel = viewModel()
    LaunchedEffect(Unit) {
        scanVM.navigationEvent.collect { event ->
            if(event == NavEvent.GoToConnection){
                navController.navigate(AppScreen.DeviceConnection.name) {
                    popUpTo(0)
                }
            }
        }
    }

    val onBackClick: () -> Unit = when (currentRoute) {
        AppScreen.DeviceConnection.name -> {
            val context = LocalContext.current
            {
                (context as? Activity)?.finish()
            }
        }

        AppScreen.Options.name -> {
            {
                scanVM.disconnect()
                navController.navigate(AppScreen.DeviceConnection.name) {
                    popUpTo(AppScreen.DeviceConnection.name) { inclusive = false }
                }
            }
        }

        else -> {
            {
                navController.navigate(AppScreen.Options.name) {
                    popUpTo(AppScreen.Options.name) { inclusive = false }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Column() {
                Banner { onBackClick() }   // your top banner
                if (!isConnectionScreen) {
                    // Show device info only when connected
                    val batteryLevel by BatteryRepository.data.collectAsState()
                    val config by ConfigRepository.data.collectAsState()
                    val devInfo by DeviceInfoRepository.deviceName.collectAsState()

                    DeviceInfoBanner(devInfo, config.pid.toString(), batteryLevel.batteryLevel)
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier
            .fillMaxSize()
        NavHost(
            navController = navController,
            startDestination = AppScreen.DeviceConnection.name,
            modifier = modifier.padding(innerPadding)
        ) {

            composable(route = AppScreen.DeviceConnection.name) {
                BackHandler { onBackClick() }
                ConnectScreen(
                    onSuccess = {
                        navController.navigate(AppScreen.Options.name)
                    },
                    modifier = modifier,
                    vm = scanVM
                )
            }
            composable(route = AppScreen.Options.name) {
                BackHandler { onBackClick() }
                val config by ConfigRepository.data.collectAsState()
                MenuScreen(
                    navController = navController,
                    modifier = modifier,
                    demoMode = config.demoMode
                )
            }
            composable(route = AppScreen.ViewData.name) {
                BackHandler { onBackClick() }
                ViewDataScreen( modifier )
            }
            composable(route = AppScreen.Configure.name) {
                BackHandler { onBackClick() }
                ConfigureScreen( modifier, navigateToMenu = {}, navigateToData = {} )
            }
            composable(route = AppScreen.NewPatient.name) {
                BackHandler { onBackClick() }
                NewPatientScreen( modifier )
            }
            composable(route = AppScreen.Console.name) {
                BackHandler { onBackClick() }
                ConsoleScreen( modifier )
            }
            composable(route = AppScreen.Demo.name) {
                BackHandler { onBackClick() }
                DemoScreen( modifier )
            }
        }
    }
}