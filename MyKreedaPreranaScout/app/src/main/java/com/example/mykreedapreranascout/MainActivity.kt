package com.example.mykreedapreranascout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mykreedapreranascout.data.AppDatabase
import com.example.mykreedapreranascout.data.AthleteRepository
import com.example.mykreedapreranascout.ui.AddAthleteScreen
import com.example.mykreedapreranascout.ui.AthleteViewModel
import com.example.mykreedapreranascout.ui.AthleteViewModelFactory
import com.example.mykreedapreranascout.ui.DashboardScreen
import com.example.mykreedapreranascout.ui.LeaderboardScreen
import com.example.mykreedapreranascout.ui.PerformanceCardScreen
import com.example.mykreedapreranascout.ui.TalentCurveScreen
import com.example.mykreedapreranascout.ui.TrialLoggerScreen
import com.example.mykreedapreranascout.ui.theme.KreedaPreranaScoutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = AthleteRepository(database.athleteDao())
        val viewModel: AthleteViewModel by viewModels {
            AthleteViewModelFactory(repository)
        }

        setContent {
            KreedaPreranaScoutTheme {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    viewModel: AthleteViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBars = currentDestination?.hierarchy?.any { it.route == "dashboard" || it.route == "leaderboard" } == true

    Scaffold(
        topBar = {
            if (showBars) {
                TopAppBar(
                    title = { Text("Kreeda-Prerana") },
                    actions = {
                        Button(
                            onClick = { navController.navigate("add_athlete") },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Register")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                Column {
                    // "Header Bottom"
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Scout Management System",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Dashboard") },
                            selected = currentDestination?.hierarchy?.any { it.route == "dashboard" } == true,
                            onClick = {
                                navController.navigate("dashboard") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            label = { Text("Leaderboard") },
                            selected = currentDestination?.hierarchy?.any { it.route == "leaderboard" } == true,
                            onClick = {
                                navController.navigate("leaderboard") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(if (showBars) paddingValues else PaddingValues(0.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(
                        viewModel = viewModel,
                        onAthleteClick = { athleteId -> navController.navigate("trial_logger/$athleteId") },
                        onPerformanceCardClick = { athleteId -> navController.navigate("performance_card/$athleteId") }
                    )
                }

                composable("add_athlete") {
                    AddAthleteScreen(
                        viewModel = viewModel,
                        onSaveClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "trial_logger/{athleteId}",
                    arguments = listOf(navArgument("athleteId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val athleteId = backStackEntry.arguments?.getInt("athleteId") ?: return@composable
                    TrialLoggerScreen(
                        athleteId = athleteId,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() },
                        onViewGraphClick = {
                            navController.navigate("talent_curve/$athleteId")
                        }
                    )
                }

                composable(
                    route = "talent_curve/{athleteId}",
                    arguments = listOf(navArgument("athleteId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val athleteId = backStackEntry.arguments?.getInt("athleteId") ?: return@composable
                    TalentCurveScreen(
                        athleteId = athleteId,
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "performance_card/{athleteId}",
                    arguments = listOf(navArgument("athleteId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val athleteId = backStackEntry.arguments?.getInt("athleteId") ?: return@composable
                    PerformanceCardScreen(
                        athleteId = athleteId,
                        viewModel = viewModel,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("leaderboard") {
                    LeaderboardScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}