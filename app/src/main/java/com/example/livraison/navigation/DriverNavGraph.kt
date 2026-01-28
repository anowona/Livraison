package com.example.livraison.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.livraison.viewmodel.DriverViewModel

@Composable
fun DriverNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val isLoggedIn = authState.user != null
    val currentUser = authState.user
    val userRole = authState.role

    LaunchedEffect(currentUser, userRole) {
        if (userRole == "livreur" && currentUser != null) {
            Log.d("AppNavigation", "Driver logged in. Fetching orders for UID: ${currentUser.uid}")
            driverViewModel.fetchOrders(currentUser.uid)
        } else {
            // Optional: You could add logic here to clear data if the user logs out
            // or is not a driver. For now, we just log it.
            Log.d("AppNavigation", "User is not a driver or is logged out. No orders will be fetched.")
        }
    }

    NavHost(navController, startDestination = "driver_dashboard") {
        composable("driver_dashboard") {
            DriverDashboardScreen(
                driverViewModel = driverViewModel,
                authViewModel = authViewModel
            )
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
