import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * LABEL: Input Form Interface (Presentation Component)
 * Designed explicitly for lower digital literacy: replaces text entry with grids and sliders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    viewModel: LoggingViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle structural state navigation actions upon successful local saving loops
    if (uiState.isSavingSuccess) {
        LaunchedEffect(Unit) {
            viewModel.resetForm()
            onNavigateBack() // Route user safely back to historical updates dashboard
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Current Symptoms", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back Button")
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "1. TAP WHAT YOU ARE FEELING:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Structured GRID layout replacing arbitrary keyword search or typing bars
                val symptomOptions = listOf("Fever", "Chills", "Headache", "Cough")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in symptomOptions.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            symptomOptions.getOrNull(i)?.let { symptom ->
                                SymptomSelectableCard(
                                    name = symptom,
                                    isSelected = uiState.selectedSymptom == symptom,
                                    modifier = Modifier.weight(1f),
                                    onSelect = { viewModel.selectSymptom(symptom) }
                                )
                            }
                            symptomOptions.getOrNull(i + 1)?.let { symptom ->
                                SymptomSelectableCard(
                                    name = symptom,
                                    isSelected = uiState.selectedSymptom == symptom,
                                    modifier = Modifier.weight(1f),
                                    onSelect = { viewModel.selectSymptom(symptom) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "2. HOW SEVERE IS IT?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                // High Contrast Discrete Slider for unambiguous structural input
                Text(
                    text = "Level: ${uiState.severityLevel} / 5",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1976D2),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Slider(
                    value = uiState.severityLevel.toFloat(),
                    onValueChange = { viewModel.updateSeverity(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3, // Restricts user to clean, explicit whole numbers (1, 2, 3, 4, 5)
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1976D2),
                        activeTrackColor = Color(0xFF2196F3)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mild (1)", fontSize = 12.sp, color = Color.Gray)
                    Text("Moderate (3)", fontSize = 12.sp, color = Color.Gray)
                    Text("Severe (5)", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Error display block handled safely inside the container view bounds
            uiState.errorMessage?.let { error ->
                Text(text = error, color = Color.Red, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Main commitment activation action block button
            Button(
                onClick = { viewModel.saveSymptomLog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Positive Action Green
            ) {
                Text("SAVE HEALTH LOG", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * LABEL: Reusable Grid Toggle Composable
 * Renders individual large tap boxes that eliminate keyboard errors.
 */
@Composable
fun SymptomSelectableCard(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier = Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF1976D2) else Color.LightGray
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF1976D2) else Color.Black
            )
        }
    }
}