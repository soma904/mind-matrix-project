package com.example.mykreedapreranascout.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: AthleteViewModel
) {
    // 1. Get the athletes using the correct variable name ('athletes', not 'allAthletes')
    val athletes by viewModel.athletes.collectAsState(initial = emptyList())

    // 2. Mathematically sort them using the function we just built
    val rankedAthletes = athletes.sortedByDescending { viewModel.calculateCompositeScore(it) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        itemsIndexed(rankedAthletes) { index, athlete ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    // Highlight the Top 3 players in a golden/yellow tint!
                    containerColor = if (index < 3) Color(0x33FFD700) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Column {
                            Text(text = athlete.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = athlete.earnedBadge ?: "Unranked", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    // Display their composite score
                    Text(
                        text = "${viewModel.calculateCompositeScore(athlete)} pts",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}