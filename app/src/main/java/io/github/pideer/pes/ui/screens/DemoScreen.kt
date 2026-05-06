package io.github.pideer.pes.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.pideer.pes.ble.repo.CalibrateRepository
import io.github.pideer.pes.ui.demoScreens.DemoActigraphScreen
import io.github.pideer.pes.ui.demoScreens.DemoBPWaitScreen
import io.github.pideer.pes.ui.demoScreens.DemoBuzzerScreen
import io.github.pideer.pes.ui.demoScreens.DemoStartScreen
import io.github.pideer.pes.ui.demoScreens.DemoTransferScreen
import io.github.pideer.pes.ui.viewmodels.DemoViewModel

enum class DemoDestination(val route: String){
    start("1. Start"),
    actigraph("2. Preparing"),
    bp_read("3. Reading Blood Pressure"),
    transfer("4. Transferring Data"),
    buzzer("5. Alert!")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScreen(
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()
    val startDestination = DemoDestination.start

    Scaffold(
        modifier = modifier,
        ) { contentPadding ->
        AppNavHost(navController, startDestination, modifier.padding(contentPadding))
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: DemoDestination,
    modifier: Modifier = Modifier,
    vm: DemoViewModel = viewModel()
) {

    // Observe the repository state
    val calibrateData by CalibrateRepository.data.collectAsState()

    // Map CalibrateServiceData.state values to destinations
    // Adjust these values to match your actual state integers/enum
    LaunchedEffect(calibrateData.state) {
        val destination = when (calibrateData.state) {
            1 -> DemoDestination.actigraph
            2 -> DemoDestination.bp_read
            3 -> DemoDestination.transfer
            4 -> DemoDestination.buzzer
            else -> null
        }
        destination?.let {
            navController.navigate(it.route) {
                launchSingleTop = true
                popUpTo(navController.currentDestination?.route ?: return@navigate) {
                    inclusive = true
                }
            }
        }
    }

val onResetClick = {
    navController.navigate(DemoDestination.start.route){
        popUpTo(0){inclusive = true}
    }
    vm.sendTrigger()
}

    NavHost(
        navController,
        startDestination = startDestination.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            )
        }
    ) {
        DemoDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    DemoDestination.start -> DemoStartScreen(
                        modifier = Modifier.fillMaxSize(),
                        onClick = {
                            navController.navigate(DemoDestination.actigraph.route)
                            vm.sendTrigger()
                        } // add .route
                    )
                    DemoDestination.actigraph -> DemoActigraphScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNext = { navController.navigate(DemoDestination.bp_read.route) },
                        onResetClick = { onResetClick() }
                    )
                    DemoDestination.bp_read -> DemoBPWaitScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNext = { navController.navigate(DemoDestination.transfer.route) },
                        onResetClick = { onResetClick() }
                    )
                    DemoDestination.transfer -> DemoTransferScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNext = { navController.navigate(DemoDestination.buzzer.route) },
                        onResetClick = { onResetClick() }
                    )
                    DemoDestination.buzzer -> DemoBuzzerScreen(
                        modifier = Modifier.fillMaxSize(),
                        onResetClick = { onResetClick() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewGraphTab(){
    val navController = rememberNavController()
    val startDestination = DemoDestination.start

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { contentPadding ->
        AppNavHost(navController, startDestination, Modifier.padding(contentPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewLiveTab(){
    val navController = rememberNavController()
    val startDestination = DemoDestination.actigraph

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { contentPadding ->
        AppNavHost(navController, startDestination, Modifier.padding(contentPadding))
    }
}