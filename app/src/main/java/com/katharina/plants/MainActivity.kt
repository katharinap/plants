package com.katharina.plants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.katharina.plants.ui.about.AboutScreen
import com.katharina.plants.ui.history.HistoryDetailScreen
import com.katharina.plants.ui.history.HistoryScreen
import com.katharina.plants.ui.plantid.PlantIdScreen
import com.katharina.plants.ui.settings.SettingsScreen
import com.katharina.plants.ui.theme.PlantsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantsTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "identify"
                ) {
                    composable("identify") {
                        PlantIdScreen(
                            viewModel = hiltViewModel(),
                            onHistoryClick = { navController.navigate("history") },
                            onAboutClick = { navController.navigate("about") },
                            onSettingsClick = { navController.navigate("settings") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = hiltViewModel(),
                            onItemClick = { id -> navController.navigate("history_detail/$id") },
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = "history_detail/{identificationId}",
                        arguments = listOf(navArgument("identificationId") { type = NavType.LongType })
                    ) {
                        HistoryDetailScreen(
                            viewModel = hiltViewModel(),
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
