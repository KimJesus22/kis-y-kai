package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.TodoItem
import com.example.ui.FoodViewModel
import com.example.ui.FoodViewModelFactory
import com.example.ui.screens.CartScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.MyApplicationTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val supabase = createSupabaseClient(
    supabaseUrl = "https://pslyuslvlpvvlptcybgh.supabase.co",
    supabaseKey = "sb_publishable_OB-auuvz5Z6Ic6-niDb1GA_fQjsRLmK"
) {
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilitar dibujo de borde a borde (edge-to-edge) con márgenes completos
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                // Inicializar el estado central del negocio / Base de datos Room mediante factoría personalizada
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
                                        // Cerrar el carrito tras la creación exitosa del pedido
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

                        composable("todo") {
                            TodoList()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoList() {
    var items by remember { mutableStateOf<List<TodoItem>>(listOf()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            items = supabase.from("todos")
                .select().decodeList<TodoItem>()
        }
    }
    LazyColumn {
        items(
            items,
            key = { item -> item.id },
        ) { item ->
            Text(
                item.name,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
