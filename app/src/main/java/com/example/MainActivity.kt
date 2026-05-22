package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.FoodViewModel
import com.example.ui.FoodViewModelFactory
import com.example.ui.screens.CartScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable full margin edge-to-edge drawing
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                // Initialize central business State / Room Database via custom factory
                val foodViewModel: FoodViewModel = viewModel(
                    factory = FoodViewModelFactory(applicationContext)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "menu"
                    ) {
                        composable("menu") {
                            MenuScreen(
                                viewModel = foodViewModel,
                                onNavigateToCart = { navController.navigate("cart") },
                                onNavigateToTracking = { navController.navigate("tracking") }
                            )
                        }
                        
                        composable("cart") {
                            CartScreen(
                                viewModel = foodViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToTracking = {
                                    navController.navigate("tracking") {
                                        // Pop cart upon successful order creation
                                        popUpTo("menu")
                                    }
                                }
                            )
                        }
                        
                        composable("tracking") {
                            TrackingScreen(
                                viewModel = foodViewModel,
                                onNavigateHome = {
                                    navController.navigate("menu") {
                                        popUpTo("menu") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
