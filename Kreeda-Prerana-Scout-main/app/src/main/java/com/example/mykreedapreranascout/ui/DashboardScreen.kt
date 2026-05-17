package com.example.mykreedapreranascout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AthleteViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onAddAthleteClick: () -> Unit,
    onAthleteClick: (Int) -> Unit,
    onLeaderboardClick: () -> Unit,
    onPerformanceCardClick: (Int) -> Unit
) {
    // 1. Grab the list of all athletes
    val athletes by viewModel.athletes.collectAsState()

    // 2. Safely track what is typed into the search bar
    var searchText by remember { mutableStateOf("") }

    // 3. Create the filtered list that dynamically updates!
    val filteredAthletes = athletes.filter { athlete ->
        athlete.name.contains(searchText, ignoreCase = true) ||
                athlete.sport.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
                TopAppBar(
                    title = { Text("Kreeda-Prerana Scout", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                    actions = {
                        TextButton(onClick = onLeaderboardClick) {
                            Text("Leaderboard")
                        }

                        IconButton(onClick = onThemeToggle) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                // --- THE SEARCH BAR ---
                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search by name or sport...") },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAthleteClick, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.Purple)
            }
        }
    ) { paddingValues ->

        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // THE CRITICAL FIX: We are now passing 'filteredAthletes' instead of 'athletes'
            items(filteredAthletes) { athlete ->
                var showMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onAthleteClick(athlete.id) }
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (athlete.photoUri != null) {
                            AsyncImage(model = athlete.photoUri, contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) {
                                Text(athlete.name.take(1).uppercase(), color = Color.Blue)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(athlete.name, style = MaterialTheme.typography.titleLarge)
                            Text("${athlete.studentClass} | ${athlete.sport}")
                        }

                        // --- EDIT / ARCHIVE MENU ---
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("View Profile Card") },
                                    onClick = { showMenu = false; onPerformanceCardClick(athlete.id) },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Archive Profile", color = Color.Green) },
                                    onClick = { showMenu = false; viewModel.archiveAthlete(athlete.id) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Green) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}