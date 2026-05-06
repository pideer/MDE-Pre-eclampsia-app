package io.github.pideer.pes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.pideer.pes.ble.repo.CalibrateRepository
import kotlinx.coroutines.launch

class DemoViewModel: ViewModel()  {
    fun sendTrigger()=
        viewModelScope.launch { CalibrateRepository.triggerDemo() }
}