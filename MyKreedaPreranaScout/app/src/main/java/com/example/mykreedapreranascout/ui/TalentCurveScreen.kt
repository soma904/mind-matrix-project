package com.example.mykreedapreranascout.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalentCurveScreen(
    athleteId: Int,
    viewModel: AthleteViewModel,
    onBackClick: () -> Unit
) {
    val athlete = viewModel.getAthleteById(athleteId)
    val trials by viewModel.getTrialsForAthlete(athleteId).collectAsState(initial = emptyList())

    // Group trials so we can pick which sport to graph
    val eventsList = trials.map { it.eventType }.distinct()
    var selectedGraphEvent by remember { mutableStateOf(if (eventsList.isNotEmpty()) eventsList[0] else "") }

    // Grab the primary theme color to style the chart
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Talent Curve: ${athlete?.name ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {

            if (trials.isEmpty()) {
                Text("No trials logged yet. Log a trial to see the Talent Curve.")
                return@Scaffold
            }

            // Tabs to switch between different sports (e.g., 100m vs Long Jump)
            ScrollableTabRow(
                selectedTabIndex = eventsList.indexOf(selectedGraphEvent).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            ) {
                eventsList.forEach { event ->
                    Tab(
                        selected = selectedGraphEvent == event,
                        onClick = { selectedGraphEvent = event },
                        text = { Text(event) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // THE MPANDROIDCHART INTEGRATION
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.textColor = onSurfaceColor
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)

                        // Style the X Axis
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.textColor = onSurfaceColor
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f

                        // Style the Y Axis
                        axisLeft.textColor = onSurfaceColor
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    // 1. Filter trials for the selected tab
                    val eventTrials = trials.filter { it.eventType == selectedGraphEvent }

                    // 2. Convert database trials into Chart "Entries"
                    val entries = eventTrials.mapIndexed { index, trial ->
                        Entry(index.toFloat(), trial.resultValue.toFloat())
                    }

                    if (entries.isNotEmpty()) {
                        // 3. Design the Line
                        val dataSet = LineDataSet(entries, "Performance Trend").apply {
                            color = primaryColor
                            valueTextColor = onSurfaceColor
                            lineWidth = 3f
                            circleRadius = 6f
                            setCircleColor(primaryColor)
                            setDrawValues(true)
                            mode = LineDataSet.Mode.CUBIC_BEZIER // Makes the line curved instead of jagged!
                        }

                        chart.data = LineData(dataSet)
                        chart.invalidate() // Triggers the chart to redraw
                    } else {
                        chart.clear()
                    }
                }
            )
        }
    }
}