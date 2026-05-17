package com.example.mykreedapreranascout.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrialLoggerScreen(
    athleteId: Int,
    viewModel: AthleteViewModel,
    onBackClick: () -> Unit,
    onViewGraphClick: () -> Unit
) {
    // Dropdown State
    val events = listOf("100m Sprint", "400m Sprint", "Long Jump", "Shot Put")
    var expanded by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf(events[0]) }

    // Logic to determine what UI to show
    val isFieldEvent = selectedEvent == "Long Jump" || selectedEvent == "Shot Put"

    // Timer states
    var timeMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }

    // Manual Input state (for jumps/throws)
    var manualResult by remember { mutableStateOf("") }

    LaunchedEffect(isRunning) {
        var lastTime = System.currentTimeMillis()
        while (isRunning) {
            delay(10)
            val currentTime = System.currentTimeMillis()
            timeMillis += (currentTime - lastTime)
            lastTime = currentTime
        }
    }

    val formattedTime = String.format(Locale.US, "%.2f", timeMillis / 1000.0)

    // THE FIX: Wrapping the screen in a Scaffold to provide a TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Trial") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // THE UNDO BUTTON
                    IconButton(onClick = { viewModel.undoLastTrial(athleteId) }) {
                        Icon(Icons.Filled.Undo, contentDescription = "Undo Last Trial")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            // Apply the Scaffold's padding so the UI doesn't hide under the top bar
            modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Record Event", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // THE EVENT DROPDOWN
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedEvent,
                    onValueChange = {},
                    readOnly = false,
                    label = { Text("Select Event") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    events.forEach { event ->
                        DropdownMenuItem(
                            text = { Text(event) },
                            onClick = {
                                selectedEvent = event
                                expanded = false
                                // Reset inputs when switching events
                                timeMillis = 0L
                                isRunning = false
                                manualResult = ""
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // DYNAMIC UI RENDERING
            if (isFieldEvent) {
                // UI for Field Events (Jumps/Throws)
                Text("Enter Distance", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = manualResult,
                    onValueChange = { manualResult = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("e.g., 4.5") },
                    suffix = { Text("meters") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // UI for Track Events (Sprints)
                // UI for Track Events (Sprints)

                // Calculates a 0.0 to 1.0 progress for a smooth sweeping animation (loops every 60 seconds)
                val sweepProgress = (timeMillis % 60000) / 60000f

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(250.dp).padding(16.dp)
                ) {
                    // The sweeping background circle
                    CircularProgressIndicator(
                        progress = { sweepProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 12.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    // The digital time display in the center
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
                // ... (Keep your Start/Stop and Reset buttons right below this!)
                Spacer(modifier = Modifier.height(48.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { isRunning = !isRunning }, modifier = Modifier.width(120.dp)) {
                        Text(if (isRunning) "Stop" else "Start")
                    }
                    OutlinedButton(
                        onClick = { timeMillis = 0L; isRunning = false },
                        modifier = Modifier.width(120.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Pushes the buttons to the bottom of the screen

            val canSave = if (isFieldEvent) manualResult.isNotBlank() else (!isRunning && timeMillis > 0)

            Button(
                onClick = {
                    val finalResult = if (isFieldEvent) {
                        manualResult.toDoubleOrNull() ?: 0.0
                    } else {
                        timeMillis / 1000.0
                    }
                    val unit = if (isFieldEvent) "m" else "s"

                    viewModel.logTrial(athleteId, selectedEvent, finalResult, unit)
                    onBackClick() // Go back to the dashboard after saving
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                enabled = canSave
            ) {
                Text("Save Result")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    // AUTO-SAVE: If the coach forgot to hit Save Result, we save it for them!
                    if (canSave) {
                        val finalResult = if (isFieldEvent) {
                            manualResult.toDoubleOrNull() ?: 0.0
                        } else {
                            timeMillis / 1000.0
                        }
                        val unit = if (isFieldEvent) "m" else "s"
                        viewModel.logTrial(athleteId, selectedEvent, finalResult, unit)
                    }
                    // Now open the graph!
                    onViewGraphClick()
                },
                modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                Text("View Talent Curve")
            }
        }
    }
}