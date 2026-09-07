package com.katharina.plants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.katharina.plants.ui.history.HistoryScreen
import com.katharina.plants.ui.plantid.PlantIdScreen
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
                            onHistoryClick = { navController.navigate("history") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = hiltViewModel(),
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
