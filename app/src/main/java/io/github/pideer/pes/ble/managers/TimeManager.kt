@file:OptIn(ExperimentalUuidApi::class)

package io.github.pideer.pes.ble.managers

import android.util.Log
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.ServiceManager
import io.github.pideer.pes.ble.ServiceManager.Companion.TAG_DATA
import io.github.pideer.pes.ble.parsers.toByteArray
import io.github.pideer.pes.ble.parsers.toTz
import io.github.pideer.pes.ble.parsers.toUnixTime
import io.github.pideer.pes.ble.repo.TimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.core.WriteType
import java.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal class TimeManager : ServiceManager {
    override val profile: Profile = Profile.TIME

    override suspend fun observeServiceInteractions(
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {

        unixTimeCharacteristic = remoteService.characteristics.firstOrNull {
            it.uuid == UNIX_TIME_CHARACTERISTIC_UUID
        } ?: throw IllegalStateException("Time characteristic not found")
//        try {
//            unixTimeCharacteristic.subscribe()
//                .mapNotNull { it.toUnixTime() }
//                .onEach { TimeRepository.updateTime(it) }
//                .onCompletion { TimeRepository.clear() }
//                .catch { e ->
//                    Log.e(TAG, "Subscribe error: ${e.message}")
//                }
//                .launchIn(scope)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Subscribe error null: ${e.message}")
//        }
        try {
            unixTimeCharacteristic
        .read().toUnixTime()
                ?.also {
                    Log.d(TAG_DATA, "Device Time: $it")
                    TimeRepository.updateTime(it)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Read error: ${e.message}")
        }

        timezoneCharacteristic = remoteService.characteristics.firstOrNull {
            it.uuid == TIMEZONE_CHARACTERISTIC_UUID
        } ?: throw IllegalStateException("Timezone characteristic not found")
        try {
            timezoneCharacteristic
                .subscribe()
                .mapNotNull { it.toTz() }
                .onEach {
                    Log.d(TAG_DATA, "Device Timezone: $it")
                    TimeRepository.updateTimezone(it)
                }
                .onCompletion { TimeRepository.clear() }
                .catch { e ->
                    Log.e(TAG, "notify read catch tz error: ${e.message}")
                }
                .launchIn(scope)
        } catch (e: Exception) {
            Log.e(TAG, "Subscribe tz error null: ${e.message}")
        }
        try {
            timezoneCharacteristic
            .read().toTz()
                ?.also {
                    Log.d(TAG_DATA, "Device Timezone: $it")
                    TimeRepository.updateTimezone(it)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Read error: ${e.message}")
        }
        val currentTime: Long = Instant.now().epochSecond
        writeTime(currentTime)
        Log.d(TAG, "Unix Time is: $currentTime")
    }

    companion object {
        const val TAG = "TimeManager"

        private val UNIX_TIME_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("043f0001-7bdb-4430-a1b9-e7d26fb2b981")
        private val TIMEZONE_CHARACTERISTIC_UUID: Uuid =
            Uuid.parse("043f0002-7bdb-4430-a1b9-e7d26fb2b981")

        private lateinit var unixTimeCharacteristic: RemoteCharacteristic
        private lateinit var timezoneCharacteristic: RemoteCharacteristic

        suspend fun writeTime(value: Long) {
            try {
                if (::unixTimeCharacteristic.isInitialized) {
                    unixTimeCharacteristic.write(value.toByteArray(), WriteType.WITH_RESPONSE)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Write error: ${e.message}")
            }
        }

        suspend fun writeTz(value: Byte) {
            val dataBytes = byteArrayOf(value)
            try {
                if (::timezoneCharacteristic.isInitialized) {
                    // Write WITHOUT response (fire-and-forget)
                    timezoneCharacteristic.write(dataBytes, WriteType.WITHOUT_RESPONSE)

                    // — OR — Write WITH response (waits for ack, can throw on failure)
                    // myWriteCharacteristic.write(data, WriteType.WITH_RESPONSE)
                    Log.d(TAG, "Write: ${dataBytes.contentToString()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Write error: ${e.message}")
            }
//            finally {
//                MyRepository.update(deviceId, value)
//            }
        }
    }
}