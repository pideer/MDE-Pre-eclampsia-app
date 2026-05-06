package io.github.pideer.pes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.pideer.pes.ui.theme.AppTheme
import io.github.pideer.pes.ui.viewmodels.NewPatientViewModel

enum class NewPatientDestination(
    val route: String,
    val label: String
) {
    ID(
        "id",
        "1. Set PID"
    ),
    Calibrate(
        "calibrate",
        "2. Calibrate"
    ),
    Configure(
        "configure",
        "3. Configure Thresholds"
    )
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NewPatientDestination,
    uiState: NewPatientViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        NewPatientDestination.entries.forEach { destination ->
            composable(destination.route) {
                when (destination) {
                    NewPatientDestination.ID -> NewPIDScreen(
                        modifier = modifier,
                        uiState.idValue(),
                    ) { uiState.setPatientId(it) }

                    NewPatientDestination.Calibrate -> CalibrateScreen({})
                    NewPatientDestination.Configure -> ConfigureScreen(
                        showViewDataButton = false,
                        navigateToMenu = {},
                        navigateToData = {})
                }
            }
        }
    }
}

@Composable
fun NewPatientScreen(
    modifier: Modifier = Modifier,
    uiState: NewPatientViewModel = viewModel(),
    startDestination: NewPatientDestination = NewPatientDestination.ID
) {
    val navController = rememberNavController()
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (selectedDestination != NewPatientDestination.Configure.ordinal) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val next = selectedDestination + 1
                        if (next < NewPatientDestination.entries.size) {
                            selectedDestination = next
                            navController.navigate(NewPatientDestination.entries[next].route)
                        }
                              },
                    icon = {
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
                    },
                    text = { Text("Next") },
                )
            }
        },

    ) { innerPadding ->
        Column {
            PrimaryTabRow(
                selectedTabIndex = selectedDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                NewPatientDestination.entries.forEachIndexed { index, destination ->
                    Tab(
                        selected = selectedDestination == index,
                        onClick = {
//                            navController.navigate(route = destination.route)
//                            selectedDestination = index
                        },
                        text = {
                            Text(
                                text = destination.label,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
            AppNavHost(navController, startDestination, uiState)
        }
    }
}

@Composable
fun NewPIDScreen(
    modifier: Modifier = Modifier,
    idValue: String = "",
    idChange: (String) -> Unit,
) {
    val isValid = idValue.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Text(
            text = "New Patient",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Enter the patient ID to initialize the device.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = idValue,
            onValueChange = idChange,
            label = { Text("Patient ID") },
            placeholder = { Text("e.g. 123456") },
            singleLine = true,
            isError = idValue.isNotBlank() && !isValid,
            supportingText = {
                if (idValue.isNotBlank() && !isValid) {
                    Text("Invalid format")
                } else {
                    Text("Changing this will erase device data")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewPatientScreenPreview() {
    val patientUiState: NewPatientViewModel = viewModel()
    patientUiState.setPatientId("12345678")

    AppTheme {
        NewPatientScreen(
            Modifier.fillMaxSize(),
            patientUiState
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewPatientBlankPreview() {
    AppTheme {
        NewPatientScreen(
            Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalibratePreview() {
    AppTheme {
        NewPatientScreen(
            Modifier.fillMaxSize(),
            startDestination = NewPatientDestination.Calibrate
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigurePreview() {
    AppTheme {
        NewPatientScreen(
            Modifier.fillMaxSize(),
            startDestination = NewPatientDestination.Configure
        )
    }
}
