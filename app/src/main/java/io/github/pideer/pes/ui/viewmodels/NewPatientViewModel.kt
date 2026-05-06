package io.github.pideer.pes.ui.viewmodels

import androidx.lifecycle.ViewModel
import io.github.pideer.pes.data.PatientData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NewPatientViewModel : ViewModel() {
    private val _state = MutableStateFlow(PatientData())
    val state: StateFlow<PatientData> = _state.asStateFlow()

    fun idValue(): String {
        return if(_state.value.patientId == null)
            ""
        else
            _state.value.patientId!!
    }

    fun setPatientId(pid: String){
        _state.update { currentState ->
            currentState.copy(patientId = pid)
        }
    }
}