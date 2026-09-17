package com.paytrack.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.paytrack.app.ui.screens.DashboardScreen
import com.paytrack.app.ui.screens.HistoryScreen
import com.paytrack.app.ui.screens.ReportScreen
import com.paytrack.app.ui.screens.SimulatorScreen
import com.paytrack.app.ui.theme.PayTrackTheme
import com.paytrack.app.ui.viewmodel.MainViewModel

enum class NavigationTab(val label: String) {
    TODAY("Today"),
    HISTORY("Past Days"),
    REPORT("Daily Report"),
    SIMULATOR("Simulator")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if opened from notification with deep link intent
        val initialTab = intent.getStringExtra("OPEN_TAB")
        val reportDate = intent.getStringExtra("REPORT_DATE")
        if (!reportDate.isNullOrBlank()) {
            viewModel.selectDate(reportDate)
        }

        setContent {
            PayTrackTheme {
                MainAppContent(
                    viewModel = viewModel,
                    initialTab = if (initialTab == "REPORT") NavigationTab.REPORT else NavigationTab.TODAY
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    initialTab: NavigationTab
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(initialTab) }

    // Check permissions
    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    var hasSmsPermissions by remember {
        mutableStateOf(
            hasPermission(Manifest.permission.RECEIVE_SMS) && hasPermission(Manifest.permission.READ_SMS)
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermissions = (permissions[Manifest.permission.RECEIVE_SMS] == true || hasPermission(Manifest.permission.RECEIVE_SMS)) &&
                            (permissions[Manifest.permission.READ_SMS] == true || hasPermission(Manifest.permission.READ_SMS))
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == NavigationTab.TODAY,
                    onClick = { currentTab = NavigationTab.TODAY },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Today") },
                    label = { Text("Today") }
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.HISTORY,
                    onClick = { currentTab = NavigationTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "Past Days") },
                    label = { Text("Past Days") }
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.REPORT,
                    onClick = { currentTab = NavigationTab.REPORT },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Daily Report") },
                    label = { Text("Report") }
                )
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SIMULATOR,
                    onClick = { currentTab = NavigationTab.SIMULATOR },
                    icon = { Icon(Icons.Default.Science, contentDescription = "Simulator") },
                    label = { Text("Simulator") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Informative Banner if SMS permission is not granted
            if (!hasSmsPermissions) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "SMS Permission Required",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                "To detect money transfers automatically, grant SMS read permissions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        TextButton(
                            onClick = {
                                val perms = mutableListOf(
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(perms.toTypedArray())
                            }
                        ) {
                            Text("Grant", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentTab) {
                    NavigationTab.TODAY -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToReports = { currentTab = NavigationTab.REPORT },
                        onNavigateToSimulator = { currentTab = NavigationTab.SIMULATOR }
                    )
                    NavigationTab.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        onDateSelectedForReport = { _ -> currentTab = NavigationTab.REPORT }
                    )
                    NavigationTab.REPORT -> ReportScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.SIMULATOR -> SimulatorScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
