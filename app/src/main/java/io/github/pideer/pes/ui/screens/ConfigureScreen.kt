package io.github.pideer.pes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.pideer.pes.ble.repo.ConfigRepository
import io.github.pideer.pes.ble.repo.TimeRepository
import io.github.pideer.pes.ui.components.ThresholdCard
import io.github.pideer.pes.ui.components.TimezoneCard
import io.github.pideer.pes.ui.theme.AppTheme
import io.github.pideer.pes.ui.viewmodels.ConfigureViewModel

@Composable
fun ConfigureScreen(
    modifier: Modifier = Modifier,
    showViewDataButton: Boolean = true,
    configureViewModel: ConfigureViewModel = viewModel(),
    navigateToMenu: () -> Unit,
    navigateToData: () -> Unit
) {
    val configState by ConfigRepository.data.collectAsState()
    val timeState by TimeRepository.data.collectAsState()
    val uiState by configureViewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        ) {
        // Title
        Text(
            text = "Configuration", style = MaterialTheme.typography.headlineMedium
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                TimezoneCard(
                    currentTimezoneValue = timeState.timezone,
                    timezoneValue = uiState.timezone,           // assuming this is now Int?
                    onTimezoneSelected = configureViewModel::setTimezone
                )
            }
            item {
                ThresholdCard(
                    currentMinSystolic = configState.systolicMin,
                    currentMinDiastolic = configState.diastolicMin,
                    currentMaxSystolic = configState.systolicMax,
                    currentMaxDiastolic = configState.diastolicMax,
                    minSystolic = uiState.minSystolic,
                    minDiastolic = uiState.minDiastolic,
                    maxSystolic = uiState.maxSystolic,
                    maxDiastolic = uiState.maxDiastolic,
                    onMinChange = configureViewModel::setMinBloodPressure,
                    onMaxChange = configureViewModel::setMaxBloodPressure
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (showViewDataButton) {
                OutlinedButton(
                    onClick = navigateToData, modifier = Modifier.weight(1f)
                ) {
                    Text("View Data")
                }
            }

            val saveEnabled =
                uiState.minSystolic != null && uiState.minDiastolic != null && uiState.maxSystolic != null && uiState.maxDiastolic != null  // add this field to your UiState
            Button(
                onClick = {
                    configureViewModel.sendConfigToDevice()
                    navigateToMenu()
                }, enabled = saveEnabled, modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.FactCheck, contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigureScreenPreview() {
    val vm: ConfigureViewModel = viewModel()
    vm.setMaxBloodPressure("130/90")
    vm.setMinBloodPressure("90/40")

//    TimeRepository.updateTimezone(-4)
//    vm.setTimezone()
    AppTheme {
        ConfigureScreen(
            Modifier.fillMaxSize(),
            configureViewModel = vm,
            navigateToData = {},
            navigateToMenu = {})
    }
}

@Preview(showBackground = true)
@Composable
fun NewPatientConfigureScreenPreview() {
    AppTheme {
        ConfigureScreen(Modifier.fillMaxSize(), false, navigateToData = {}, navigateToMenu = {})
    }
}