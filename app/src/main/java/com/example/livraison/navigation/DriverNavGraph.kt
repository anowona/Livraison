package com.example.livraison.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.livraison.ui.screens.DriverDashboardScreen
import com.example.livraison.ui.screens.LoginScreen
import com.example.livraison.ui.screens.ProfileScreen
import com.example.livraison.viewmodel.AuthViewModel

@Composable
fun DriverNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val isLoggedIn = authState.user != null

    NavHost(navController, startDestination = "driver_dashboard") {
        composable("driver_dashboard") {
            DriverDashboardScreen()
        }
        composable("profile") { // Added profile route
            if (isLoggedIn) {
                // Create a ProfileScreen for logged in users
                ProfileScreen(authViewModel, navController)
            } else {
                LoginScreen(authViewModel, navController)
            }
        }
    }
}
