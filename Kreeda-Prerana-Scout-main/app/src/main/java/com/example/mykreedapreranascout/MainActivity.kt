package com.example.mykreedapreranascout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

        // 4. Draw the User Interface!

        setContent {
            // 1. Create a state variable to track the theme
            var isDarkTheme by remember { mutableStateOf(false) }

            // 2. Pass the variable into your theme
            KreedaPreranaScoutTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                isDarkTheme = isDarkTheme, // Pass the current state
                                onThemeToggle = { isDarkTheme = !isDarkTheme }, // Pass the toggle action
                                onAddAthleteClick = { navController.navigate("add_athlete") },
                                onAthleteClick = { athleteId -> navController.navigate("trial_logger/$athleteId") },
                                onLeaderboardClick = { navController.navigate("leaderboard") },
                                onPerformanceCardClick = { athleteId -> navController.navigate("performance_card/$athleteId") }
                            )
                        }
                        // ... (Leave the rest of your NavHost routes exactly as they are)

                        // Route 2: Add Athlete
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
                        // Route 5: Talent Curve (NEW)
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
                        // Route 6: Performance Card (NEW)
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

                        // Route 4: Leaderboard (NEW)
                        composable("leaderboard") {
                            LeaderboardScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}