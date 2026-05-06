package io.github.pideer.pes.ble.managers

import android.util.Log
import io.github.pideer.pes.ble.Profile
import io.github.pideer.pes.ble.ServiceManager
import io.github.pideer.pes.ble.ServiceManager.Companion.TAG_DATA
import io.github.pideer.pes.ble.parsers.toBoolean
import io.github.pideer.pes.ble.parsers.toByteArray
import io.github.pideer.pes.ble.parsers.toFloatList
import io.github.pideer.pes.ble.parsers.toMinMaxList
import io.github.pideer.pes.ble.parsers.toUInt
import io.github.pideer.pes.ble.repo.ConfigRepository
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
class ConfigManager : ServiceManager {
    override val profile: Profile = Profile.CONFIG

    override suspend fun observeServiceInteractions(
        remoteService: RemoteService,
        scope: CoroutineScope
    ) {
        try {
            remoteService.characteristics.firstOrNull {
                it.uuid == DEMO_MODE_CHARACTERISTIC_UUID
            }
                ?.read()?.toBoolean()
                ?.also {
                    Log.d(TAG_DATA, "demo mode = $it")
                    ConfigRepository.updateDemoMode(it)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init: ${e.toString()}")
        }

        try {
            pidCharacteristic = remoteService.characteristics.firstOrNull {
                it.uuid == PID_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("Characteristic not found")
            pidCharacteristic
                .read().toUInt()
                .also {
                    Log.d(TAG_DATA, "PID = $it")
                    ConfigRepository.updatePID(it)
                }

            pidCharacteristic
                .subscribe()
                .mapNotNull { it.toUInt() }
                .onEach {
                    Log.d(TAG_DATA, "PID: $it")
                    ConfigRepository.updatePID(it)
                }
                .onCompletion { ConfigRepository.clear() }
                .catch { e ->
                    Log.e(TAG, e.toString())
                }
                .launchIn(scope)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init: ${e.toString()}")
        }

        try {
            thresholdCharacteristic = remoteService.characteristics.firstOrNull {
                it.uuid == THRESHOLD_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("Characteristic not found")
            thresholdCharacteristic
                .subscribe()
                .mapNotNull { it.toMinMaxList() }
                .onEach {
                    Log.d(TAG_DATA, "Systolic Min = ${it[0]}")
                    Log.d(TAG_DATA, "Systolic Max = ${it[1]}")
                    Log.d(TAG_DATA, "Diastolic Min = ${it[2]}")
                    Log.d(TAG_DATA, "Diastolic Max = ${it[3]}")
                    ConfigRepository.updateThresholds(it)
                }
                .onCompletion { ConfigRepository.clear() }
                .catch { e ->
                    Log.e(BatteryManager.TAG, e.toString())
                }
                .launchIn(scope)

            thresholdCharacteristic
                .read().toMinMaxList()
                .also {
                    Log.d(TAG_DATA, "Systolic Min = ${it[0]}")
                    Log.d(TAG_DATA, "Systolic Max = ${it[1]}")
                    Log.d(TAG_DATA, "Diastolic Min = ${it[2]}")
                    Log.d(TAG_DATA, "Diastolic Max = ${it[3]}")
                    ConfigRepository.updateThresholds(it)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init: ${e.toString()}")
        }

//        try {
//            systolicCharacteristic = remoteService.characteristics.firstOrNull {
//                it.uuid == SYSTOLIC_CHARACTERISTIC_UUID
//            } ?: throw IllegalStateException("Characteristic not found")
//            systolicCharacteristic
//                .subscribe()
//                .mapNotNull { it.toMinMaxList() }
//                .onEach {
//                    Log.d(TAG_DATA, "Systolic Min = ${it[0]}")
//                    Log.d(TAG_DATA, "Systolic Max = ${it[1]}")
//                    ConfigRepository.updateSystolic(it)
//                }
//                .onCompletion { ConfigRepository.clear() }
//                .catch { e ->
//                    Log.e(TAG, e.toString())
//                }
//                .launchIn(scope)
//
//            systolicCharacteristic
//                .read().toMinMaxList()
//                .also {
//                    Log.d(TAG_DATA, "Systolic Min = ${it[0]}")
//                    Log.d(TAG_DATA, "Systolic Max = ${it[1]}")
//                    ConfigRepository.updateSystolic(it)
//                }
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to init: ${e.toString()}")
//        }

        try {
            coefficientsCharacteristic = remoteService.characteristics.firstOrNull {
                it.uuid == COEFFICIENTS_CHARACTERISTIC_UUID
            } ?: throw IllegalStateException("Characteristic not found")
            coefficientsCharacteristic
                .subscribe()
                .mapNotNull { it.toFloatList() }
                .onEach {
                    Log.d(TAG_DATA, "Systolic Coefficient M = ${it[0]}")
                    Log.d(TAG_DATA, "Systolic Coefficient B = ${it[1]}")
                    Log.d(TAG_DATA, "Diastolic Coefficient M = ${it[2]}")
                    Log.d(TAG_DATA, "Diastolic Coefficient B = ${it[3]}")
                    ConfigRepository.updateCoefficients(it)
                }
                .onCompletion { ConfigRepository.clear() }
                .catch { e ->
                    Log.e(TAG, e.toString())
                }
                .launchIn(scope)

            coefficientsCharacteristic
                .read().toFloatList()
                .also {
                    Log.d(TAG_DATA, "Systolic Coefficient M = ${it[0]}")
                    Log.d(TAG_DATA, "Systolic Coefficient B = ${it[1]}")
                    Log.d(TAG_DATA, "Diastolic Coefficient M = ${it[2]}")
                    Log.d(TAG_DATA, "Diastolic Coefficient B = ${it[3]}")
                    ConfigRepository.updateCoefficients(it)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init: ${e.toString()}")
        }
//
//        try {
//            systolicCoefficientsCharacteristic = remoteService.characteristics.firstOrNull {
//                it.uuid == SYSTOLIC_COEFFICIENTS_CHARACTERISTIC_UUID
//            } ?: throw IllegalStateException("Characteristic not found")
//            systolicCoefficientsCharacteristic
//                .subscribe()
//                .mapNotNull { it.toFloatList() }
//                .onEach {
//                    Log.d(TAG_DATA, "Systolic Coefficient M = ${it[0]}")
//                    Log.d(TAG_DATA, "Systolic Coefficient B = ${it[1]}")
//                    ConfigRepository.updateSystolicCoefficients(it)
//                }
//                .onCompletion { ConfigRepository.clear() }
//                .catch { e ->
//                    Log.e(TAG, e.toString())
//                }
//                .launchIn(scope)
//
//            systolicCoefficientsCharacteristic
//                .read().toFloatList()
//                .also {
//                    Log.d(TAG_DATA, "Systolic Coefficient M = ${it[0]}")
//                    Log.d(TAG_DATA, "Systolic Coefficient B = ${it[1]}")
//                    ConfigRepository.updateSystolicCoefficients(it)
//                }
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to init: ${e.toString()}")
//        }
//
//        try {
//            newPatientCharacteristic = remoteService.characteristics.firstOrNull {
//                it.uuid == NEW_PATIENT_CHARACTERISTIC_UUID
//            } ?: throw IllegalStateException("Characteristic not found")
//        } catch (e: Exception) {
//            Log.e(TAG, "Failed to init: ${e.toString()}")
//        }
    }


    companion object {
        private const val TAG = "ConfigManager"

        private val DEMO_MODE_CHARACTERISTIC_UUID =
            Uuid.parse("32610001-7bdb-4430-a1b9-e7d26fb2b981")
//        private val NEW_PATIENT_CHARACTERISTIC_UUID =
//            Uuid.parse("32610002-7bdb-4430-a1b9-e7d26fb2b981")
        private val PID_CHARACTERISTIC_UUID = Uuid.parse("32610003-7bdb-4430-a1b9-e7d26fb2b981")
        private val THRESHOLD_CHARACTERISTIC_UUID =
            Uuid.parse("32610004-7bdb-4430-a1b9-e7d26fb2b981")
//        private val SYSTOLIC_CHARACTERISTIC_UUID =
//            Uuid.parse("32610005-7bdb-4430-a1b9-e7d26fb2b981")
        private val COEFFICIENTS_CHARACTERISTIC_UUID =
            Uuid.parse("32610006-7bdb-4430-a1b9-e7d26fb2b981")
//        private val SYSTOLIC_COEFFICIENTS_CHARACTERISTIC_UUID =
//            Uuid.parse("32610007-7bdb-4430-a1b9-e7d26fb2b981")

//        private lateinit var newPatientCharacteristic: RemoteCharacteristic
        private lateinit var pidCharacteristic: RemoteCharacteristic
        private lateinit var thresholdCharacteristic: RemoteCharacteristic
//        private lateinit var systolicCharacteristic: RemoteCharacteristic
        private lateinit var coefficientsCharacteristic: RemoteCharacteristic
//        private lateinit var systolicCoefficientsCharacteristic: RemoteCharacteristic

//        suspend fun writeNewPatient() {
//            try {
//                if (::newPatientCharacteristic.isInitialized)
//                    newPatientCharacteristic.write(byteArrayOf(1.toByte()), WriteType.WITH_RESPONSE)
//            } catch (e: Exception) {
//                Log.e(TAG, "failed to write ${e.toString()}")
//            }
//        }

        suspend fun writePID(pid: Int) {
            try {
                if (::pidCharacteristic.isInitialized)
                    pidCharacteristic.write(pid.toByteArray(), WriteType.WITH_RESPONSE)
            } catch (e: Exception) {
                Log.e(TAG, "failed to write ${e.toString()}")
            }
        }

        suspend fun writeThreshold(values: List<Int>) {
            try {
                if (::thresholdCharacteristic.isInitialized)
                    thresholdCharacteristic.write(


                            values.map { it.toByte() }.toByteArray()
                        , WriteType.WITH_RESPONSE
                    )
            } catch (e: Exception) {
                Log.e(TAG, "failed to write ${e.toString()}")
            }
        }

        suspend fun writeCoefficients(values: List<Float>) {
            try {
                if (::coefficientsCharacteristic.isInitialized)
                    coefficientsCharacteristic.write(
                        values[0].toByteArray() + values[1].toByteArray() + values[2].toByteArray() + values[3].toByteArray(),
                        WriteType.WITH_RESPONSE
                    )
            } catch (e: Exception) {
                Log.e(TAG, "failed to write ${e.toString()}")
            }
        }

//        suspend fun writeSystolic(values: List<Int>) {
//            try {
//                if (::systolicCharacteristic.isInitialized)
//                    systolicCharacteristic.write(
//                        byteArrayOf(
//                            values[0].toByte(),
//                            values[1].toByte()
//                        ), WriteType.WITH_RESPONSE
//                    )
//            } catch (e: Exception) {
//                Log.e(TAG, "failed to write ${e.toString()}")
//            }
//        }
//
//        suspend fun writeSystolicCoefficients(values: List<Float>) {
//            try {
//                if (::systolicCoefficientsCharacteristic.isInitialized)
//                    systolicCoefficientsCharacteristic.write(
//                        values[0].toByteArray() + values[1].toByteArray(),
//                        WriteType.WITH_RESPONSE
//                    )
//            } catch (e: Exception) {
//                Log.e(TAG, "failed to write ${e.toString()}")
//            }
//        }
    }
}