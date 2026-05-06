package io.github.pideer.pes.ui.demoScreens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DemoBuzzerScreen(
    modifier: Modifier = Modifier,
    onResetClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        OutlinedButton(
            onClick = onResetClick,
            shape = CircleShape,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.size(200.dp)
        ) {
            Text(
                text = "Restart",
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDemoBuzzer() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { contentPadding ->
        DemoBuzzerScreen(Modifier.padding(contentPadding).fillMaxSize(),
            { } )
    }
}