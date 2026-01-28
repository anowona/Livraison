package com.example.livraison.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.livraison.ui.screens.*
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.DriverViewModel
import com.example.livraison.viewmodel.MainViewModel

@Composable
fun NavGraph(vm: MainViewModel, navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()

    val authState by authViewModel.uiState.collectAsState()
    val isLoggedIn = authState.user != null

    NavHost(navController, startDestination = "routing") {
        composable("routing") {
            RoutingScreen(navController)
        }
        composable("login") {
            if (isLoggedIn) {
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            } else {
                LoginScreen(authViewModel, navController)
            }
        }
        composable("register") {
            if (isLoggedIn) {
                navController.navigate("home") { popUpTo("register") { inclusive = true } }
            } else {
                RegisterScreen(authViewModel, navController)
            }
        }
        composable("role_selection") {
            RoleSelectionScreen(navController)
        }
        composable("home") {
            val userRole = authState.role
            if (userRole == "driver") {
                DriverDashboardScreen(
                    driverViewModel = driverViewModel,
                    authViewModel = authViewModel
                )
            } else {
                HomeScreen(vm) {
                    navController.navigate("cart")
                }
            }
        }
        composable("cart") {
            val onCheckout = {
                if (isLoggedIn) {
                    navController.navigate("tracking")
                } else {
                    navController.navigate("login")
                }
            }
            CartScreen(vm, authViewModel, navController, onCheckout)
        }

        composable("map") {
            OSMMapScreen()
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