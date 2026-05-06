package io.github.pideer.pes.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.pideer.pes.MainApplication
import io.github.pideer.pes.ble.ConnectState
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.repo.ConfigRepository
import io.github.pideer.pes.ble.repo.DeviceInfoRepository
import io.github.pideer.pes.ble.repo.TransferRepository
import io.github.pideer.pes.ui.state.NavEvent
import io.github.pideer.pes.ui.state.ScanUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.distinctByPeripheral
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "ScanViewModel"

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    val centralManager: CentralManager = (application as MainApplication).centralManager

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private val _peripherals: MutableStateFlow<List<Peripheral>> = MutableStateFlow(emptyList())
    val peripherals = _peripherals.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavEvent> = _navigationEvent.asSharedFlow()

    private var scanJob: Job? = null
    private var connectJob: Job? = null

    init {
        centralManager.state
            .onEach { state ->
                _uiState.update { it.copy(bluetoothState = state) }
            }
            .launchIn(viewModelScope)
    }

    fun startScan() {
        if (_uiState.value.isScanning) {
            Log.d(TAG, "is scanning. exit")
            return
        }

        // can scan
        scanJob = centralManager
            .scan(Duration.INFINITE) {
                Any {
                    Name(Regex("PES.*"))
                }
            }
            .onStart {
                Log.d(TAG, "Scan started")
                _uiState.update { currentState ->
                    currentState.copy(
                        isScanning = true,
                        error = null
                    )
                }
            }
          .distinctByPeripheral()
            .map { it.peripheral }
            .filterNot { _peripherals.value.contains(it) }
            //.distinct()
            .onEach { newPeripheral ->
                Log.d(TAG, "Found new device: ${newPeripheral.name} (${newPeripheral.address})")
                _peripherals.update { peripherals.value + newPeripheral }
            }
            .catch { e ->
                Log.e(TAG, "Scan error: $e")
                _uiState.update { currentState ->
                    currentState.copy(
                        error = e.message
                    )
                }
            }
            .onCompletion {
                Log.d(TAG, "Scan Completed")
                _uiState.update { currentState ->
                    currentState.copy(
                        isScanning = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun stopScan() {
        Log.d(TAG, "Stop Scanning. should be leaving page")
        scanJob?.cancel()
        _uiState.update { it.copy(isScanning = false) }
    }

    fun onPeripheralSelected(peripheral: Peripheral) {
        Log.d(TAG, "selected periph")
        _uiState.update { it.copy(selectedPeripheral = peripheral) }

        // Cancel any ongoing connection attempt before starting a new one
        connectJob?.cancel()
        connectJob = viewModelScope.launch { // Use the appScope passed in the constructor
            try {
                _uiState.update { it.copy(connectState = ConnectState.Connecting) }
                Log.e("connectstate", "Connectstate connecting")

                // Note: registerServices must be launched in a way that can be cancelled
                registerServices(peripheral, this)

                centralManager.connect(
                    peripheral = peripheral,
                    options = CentralManager.ConnectionOptions.Direct(
                        automaticallyRequestHighestMtu = true
                    )
                )

                DeviceInfoRepository.setDeviceName(peripheral.name)

                // Wait for specific data before declaring "Connected"
                ConfigRepository.data.first { it.pid != null }
                _uiState.update { it.copy(connectState = ConnectState.Connected) }
                Log.e("connectstate", "Connectstate connected")
                // start transfer
                TransferRepository.trigger()
                viewModelScope.launch {
                    _navigationEvent.emit(NavEvent.GoToMenu)
//                    _uiState.value.selectedPeripheral?.state?.collect { state ->
//                        if (state == ConnectionState.Disconnected()) {
//                            Log.e(TAG, "Unexpected disconnect!")
//
//                            disconnect()
//                        }
//                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connect failed", e)
                _uiState.update { it.copy(connectState = ConnectState.Failed) }
                Log.e("connectstate", "Connectstate failed")
            }
        }
    }

    fun disconnect(){
        val selected = _uiState.value.selectedPeripheral
        connectJob?.cancel()

        viewModelScope.launch {
            selected?.let { peripheral ->
                try {
                    peripheral.disconnect()
                    Log.d(TAG, "Disconnected ${peripheral.name}")
                } catch (e: Exception) {
                    Log.w(TAG, "Error during disconnect", e)
                }
            }
            resetConnectionState()
            _uiState.update { it.copy(selectedPeripheral = null) }
//            _navigationEvent.emit(NavEvent.GoToConnection)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun registerServices(peripheral: Peripheral, scope: CoroutineScope) {
        Profile.entries.forEach { profile ->
            peripheral.profile(
                serviceUuid = profile.uuid,
                required = false
            ) { remoteService ->
                profile.createManager()
                    .let {
                        Log.d(TAG, "Service found, starting manager for $profile")
                        it.observeServiceInteractions(remoteService, scope)
                        awaitCancellation()
                    }

            }
        }
    }

    fun resetConnectionState() {
        _uiState.update { it.copy(connectState = ConnectState.Idle) }
        Log.e("connectstate", "Connectstate idle")
    }

    public override fun onCleared() {
        stopScan()
        super.onCleared()
    }
}