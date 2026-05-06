package io.github.pideer.pes.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.pideer.pes.ble.repo.TransferRepository
import io.github.pideer.pes.data.Payload
import io.github.pideer.pes.ui.components.ConsoleCard

@Composable
fun ConsoleScreen(
    modifier: Modifier = Modifier
){
    val entries by TransferRepository.debug.collectAsState()
    LazyColumn(modifier) {
        items(entries
            .sortedBy { it.header.timestamp }
        ){ entry ->
            ConsoleCard(
                entry.header.timestamp,
                (entry.payload as? Payload.Debug)?.msg ?: ""
                )
        }
    }
}