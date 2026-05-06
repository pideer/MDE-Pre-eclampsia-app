package io.github.pideer.pes.ble.managers

import android.util.Log
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.ServiceManager
import io.github.pideer.pes.ble.parsers.toBoolean
import io.github.pideer.pes.ble.parsers.toFloat
import io.github.pideer.pes.ble.repo.CalibrateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.WriteType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CalibrateManager : ServiceManager {
    override val profile: Profile = Profile.CALIBRATE

    override suspend fun observeServiceInteractions(
        remoteService: RemoteService, scope: CoroutineScope
    ) {
        sscope = scope

        streamCharacteristic = remoteService.characteristics.firstOrNull{
            it.uuid == STREAM_CHARACTERISTIC_UUID
        } ?: throw IllegalStateException("Stream characteristic not found")

        triggerCharacteristic = remoteService.characteristics.firstOrNull{
            it.uuid == TRIGGER_CHARACTERISTIC_UUID
        } ?: throw IllegalStateException("Stream Trigger characteristic not found")
        try{
            triggerCharacteristic.subscribe()
                .mapNotNull {
                    it.toBoolean()
                }
                .onEach {
                    Log.d(TAG, "Next State")
                    CalibrateRepository.incrementDemoState()
                }
                .onCompletion {
                    CalibrateRepository.clear()
                }
                .catch { e ->
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                }
                .launchIn(scope)
        }catch(e: Exception){
            Log.e(TAG, "failed to subscribe to calibrate trigger $e")
        }
    }

    companion object {
        const val TAG = "CalibrateManager"
        private val STREAM_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("c16e0002-7bdb-4430-a1b9-e7d26fb2b981")

        private val TRIGGER_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("c16e0001-7bdb-4430-a1b9-e7d26fb2b981")

        private lateinit var streamCharacteristic: RemoteCharacteristic
        private lateinit var triggerCharacteristic: RemoteCharacteristic
        private lateinit var sscope: CoroutineScope

        suspend fun writeTrigger(){
            try {
                if(::triggerCharacteristic.isInitialized){
                    triggerCharacteristic.write(byteArrayOf(0x01), WriteType.WITHOUT_RESPONSE)
                    Log.d(TAG, "trigger sent")
                }
            } catch(e: Exception){
                Log.e(TAG, "Trigger Write error: ${e.message}")
            }
        }

        suspend fun startStream(){
            try {
                if (::streamCharacteristic.isInitialized) {
                    Log.d(TAG, "Starting Stream")
                    streamCharacteristic.subscribe()
                        .mapNotNull {
                            it.toFloat()
                        }
                        .onEach {
                            Log.d(TAG, "value is $it.")
                            CalibrateRepository.addStreamItem(it)
                        }
                        .onCompletion {
                            CalibrateRepository.clear()
                        }
                        .catch { e ->
                            e.printStackTrace()
                            Log.e(TAG, e.toString())
                        }
                        .launchIn(sscope)
                }
            } catch (e: Exception){
                Log.d(TAG, "Failed to start Stream $e")
            }
        }

        suspend fun stopStream() {
            Log.d(TAG, "Ending Stream")
            try {
                streamCharacteristic.descriptors
                    .firstOrNull {
                        it.uuid.toString().lowercase() == "00002902-0000-1000-8000-00805f9b34fb"
                    }
                    ?.write(byteArrayOf(0x00, 0x00))
            } catch (e: Exception){
                Log.d(TAG, "Failed to end Stream")
            }
        }
    }
}