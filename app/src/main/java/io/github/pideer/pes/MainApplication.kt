package io.github.pideer.pes

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.native
import no.nordicsemi.kotlin.ble.environment.android.NativeAndroidEnvironment

class MainApplication : Application() {
    lateinit var environment: NativeAndroidEnvironment
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var centralManager: CentralManager
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("MainApplication", "Creating NativeAndroidEnvironment")
        environment = NativeAndroidEnvironment.getInstance(
            context = this,
            isNeverForLocationFlagSet = true   // we don't use BLE for location
        )
        centralManager = CentralManager.native(
            environment,
            appScope
        )
    }

    override fun onTerminate() {
        super.onTerminate()
        environment.close()   // unregisters BT state BroadcastReceiver
        appScope.cancel()
    }
}