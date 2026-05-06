package io.github.pideer.pes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.pideer.pes.data.ConsoleItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConsoleViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ConsoleItem>>(emptyList<ConsoleItem>())
    val items: StateFlow<List<ConsoleItem>> = _items.asStateFlow()

    fun add(item: ConsoleItem){
        viewModelScope.launch {
            _items.value += item
        }
    }

    fun listOfConsoleItems(): List<ConsoleItem> = items.value

}