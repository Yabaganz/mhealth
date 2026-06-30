import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

/**
 * LABEL: UI View (Presentation Component)
 * Renders the main dashboard view based on the state emitted by the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogNewSymptomClick: () -> UserInterfaceNavigationTarget
) {
    // Observe the StateFlow from the ViewModel. The UI automatically recomposes when state changes.
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("mHealth Symptom Logger", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2)),
                actions = {
                    // Objective 2 persistent visual confirmation of database security status
                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Offline-Safe",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Accessibility Header Greeting
            Text(
                text = "Welcome back, Chidi!\nTrack your health offline.",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            // Objective 4 Primary Action Button: Large tap target zone for easy physical access
            Button(
                onClick = { onLogNewSymptomClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("LOG NEW SYMPTOMS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "HEALTH LOG HISTORY",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Handle asynchronous data states emitted by the database engine
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2196F3))
                    }
                }
                uiState.errorMessage != null -> {
                    Text(text = uiState.errorMessage ?: "Unknown error", color = Color.Red, modifier = Modifier.weight(1f))
                }
                uiState.healthHistory.isEmpty() -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("No symptoms logged yet.", color = Color.Gray, fontSize = 16.sp)
                    }
                }
                else -> {
                    // LazyColumn optimizes rendering memory on low-tier smartphones by recycling view components
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.healthHistory) { log ->
                            SymptomHistoryCard(log = log)
                        }
                    }
                }
            }
        }
    }
}

/**
 * LABEL: UI Reusable Element Component
 * Renders an individual historical card utilizing high-contrast color codes for medical severity.
 */
@Composable
fun SymptomHistoryCard(log: SymptomLogEntity) {
    // Map triage levels to explicit contrast colors
    val indicatorColor = when {
        log.severityLevel >= 4 -> Color(0xFFD32F2F) // Critical: Dark Red
        log.severityLevel == 3 -> Color(0xFFF57C00) // Moderate: Amber Orange
        else -> Color(0xFF388E3C)                   // Stable: Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual alert icon mapping directly to the processed risk severity level
            Icon(
                imageVector = if (log.severityLevel >= 4) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = "Status Icon",
                tint = indicatorColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Human-readable simple timestamp parsing
                val dateString = SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
                    .format(Date(log.timestamp))

                Text(text = dateString, fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "${log.symptomName} (Level ${log.severityLevel}/5)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (log.isSynced) "Synced | Cloud Secured" else "Alerted | Saved Locally",
                    fontSize = 12.sp,
                    color = if (log.isSynced) Color(0xFF388E3C) else Color(0xFFF57C00)
                )
            }
        }
    }
}

// Sealed target construct indicating safe view destination handling types
sealed interface UserInterfaceNavigationTarget {
    object NavigateToLoggingForm : UserInterfaceNavigationTarget
}