package com.example.weathery.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            // Set status bar color to match app theme
            SideEffect {
                window.statusBarColor = Color(0xFF181B23).toArgb()
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = false
            }
            WeatherAppComposeRoot()
        }
    }
}

@Composable
fun WeatherAppComposeRoot() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            WeatherMainScreen(
                onCityClick = { cityName ->
                    navController.navigate("detail/$cityName")
                },
                onLocationResult = { latLng ->
                    navController.navigate("detail/$latLng")
                }
            )
        }
        composable("detail/{city}") { backStackEntry ->
            val city = backStackEntry.arguments?.getString("city") ?: ""
            WeatherDetailScreen(
                city = city,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
