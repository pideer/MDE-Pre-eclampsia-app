package io.github.pideer.pes.ble.managers

import android.util.Log
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.ServiceManager
import io.github.pideer.pes.ble.ServiceManager.Companion.TAG_DATA
import io.github.pideer.pes.ble.parsers.parseTransferData
import io.github.pideer.pes.ble.parsers.toInt
import io.github.pideer.pes.ble.repo.TransferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TransferManager : ServiceManager {
    override val profile: Profile = Profile.TRANSFER

    override suspend fun observeServiceInteractions(
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        sscope = scope
        dataCharacteristic = remoteService.characteristics.firstOrNull {
            it.uuid == DATA_CHARACTERISTIC_UUID
        } ?: throw IllegalStateException("Data characteristic not found")

        try {
            remoteService.characteristics
                .firstOrNull { it.uuid == DATA_SIZE_CHARACTERISTIC_UUID }
                ?.subscribe()
                ?.mapNotNull { it.toInt() }
                ?.onEach { size ->
                    Log.d(TAG_DATA, "Transfer Size: $size")
                    // Explicitly write 0x0000 to the CCCD to tell the device to stop notifying
                    dataCharacteristic.descriptors
                        .firstOrNull { it.uuid.toString().lowercase() == "00002902-0000-1000-8000-00805f9b34fb" }
                        ?.write(byteArrayOf(0x00, 0x00))
                    // validate length
                    TransferRepository.checkSize(size)
                }
                ?.onCompletion { TransferRepository.clear() }
                ?.catch { e ->
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                }
                ?.launchIn(scope)
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to transfer: ${e.toString()}")
        }

//        try {
//            triggerTransfer()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error subscribing to transfer: ${e.toString()}")
//        }
    }

    companion object {
        const val TAG = "TransferManager"

        private val TRIGGER_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("8d760001-7bdb-4430-a1b9-e7d26fb2b981")
        private val DATA_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("8d760002-7bdb-4430-a1b9-e7d26fb2b981")
        private val DATA_SIZE_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("8d760003-7bdb-4430-a1b9-e7d26fb2b981")

        private lateinit var triggerCharacteristic: RemoteCharacteristic
        private lateinit var dataCharacteristic: RemoteCharacteristic

        private lateinit var sscope: CoroutineScope

        fun triggerTransfer() {
            if (!::dataCharacteristic.isInitialized) return
            try {
                dataCharacteristic.subscribe()
                    .mapNotNull {
                        Log.d(TAG, "Incoming Size: ${it.size}")
                        it.parseTransferData()
                    }
                    .onEach {
                        Log.d("BleData", "Type:${it.header.type} Payload: ${it.payload}")
                        TransferRepository.add(it)
                    }
                    .onCompletion { TransferRepository.clear() }
                    .catch { e ->
                        e.printStackTrace()
                        Log.e(TAG, e.toString())
                    }
                    .launchIn(sscope)
            } catch (e: Exception) {
                Log.e(TAG, "triggerTransfer error: ${e.message}")
            }
        }
    }
}