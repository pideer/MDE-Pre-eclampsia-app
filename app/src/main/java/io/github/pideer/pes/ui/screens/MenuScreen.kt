package io.github.pideer.pes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SsidChart
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.github.pideer.pes.AppScreen
import io.github.pideer.pes.ui.components.MenuCard
import io.github.pideer.pes.ui.theme.AppTheme

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier, navController: NavController, demoMode: Boolean = false
) {
    Box(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {

            item {
                Text(
                    text = "MAIN MENU",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
            }

            item {
                MenuCard(
                    "View Data",
                    "Browse recorded results",
                    Icons.Outlined.SsidChart,
                    { navController.navigate(AppScreen.ViewData.name) })
            }

            item {
                MenuCard(
                    "New Patient",
                    "Start a new screening session",
                    Icons.Outlined.PersonAdd,
                    { navController.navigate(AppScreen.NewPatient.name) })
            }

            item {
                MenuCard(
                    "Configure Device",
                    "Adjust app settings",
                    Icons.Outlined.Settings,
                    { navController.navigate(AppScreen.Configure.name) })
            }
            item {
                Spacer(Modifier.size(5.dp))
            }

            if (demoMode) {
                item {
                    Text(
                        text = "DEVELOPER OPTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, bottom = 4.dp)
                    )
                }

                item {
                    MenuCard(
                        "Console",
                        "Developer debug console",
                        Icons.Outlined.Terminal,
                        { navController.navigate(AppScreen.Console.name) },
                        isDemo = true
                    )
                }

                item {
                    MenuCard(
                        "Demo Mode",
                        "Run a guided demonstration",
                        Icons.Outlined.PlayCircle,
                        { navController.navigate(AppScreen.Demo.name) },
                        isDemo = true
                    )
                }

                item {
                    Spacer(Modifier.size(2.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    AppTheme {
        MenuScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            demoMode = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DemoMenuScreenPreview() {
    AppTheme {
        MenuScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            demoMode = true
        )
    }
}