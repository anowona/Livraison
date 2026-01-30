package com.example.livraison.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.livraison.ui.screens.*
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.DriverViewModel
import com.example.livraison.viewmodel.MainViewModel

@Composable
fun ClientNavGraph(vm: MainViewModel, navController: NavHostController) {
    val driverViewModel: DriverViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val isLoggedIn = authState.user != null

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(vm) {
                navController.navigate("cart")
            }
        }
        composable("cart") {
            val onCheckout = {
                if (isLoggedIn) {
                    navController.navigate("tracking") {
                        popUpTo("cart") { inclusive = true } 
                    }
                } else {
                    navController.navigate("login")
                }
            }
            CartScreen(vm, authViewModel, navController, onCheckout)
        }
        composable(
            "tracking?orderId={orderId}",
            arguments = listOf(navArgument("orderId") { nullable = true })
        ) {
            val orderId = it.arguments?.getString("orderId")
            val currentUserId = authState.user?.uid
            if (!currentUserId.isNullOrBlank()) {
                OrderTrackingScreen(vm, navController, currentUserId, orderId)
            }
        }
        composable("map") {
            OSMMapScreen()
        }
        composable("profile") { 
            if (isLoggedIn) {
                ProfileScreen(authViewModel, navController)
            } else {
                LoginScreen(authViewModel, navController)
            }
        }
        composable("order_history") {
            OrderHistoryScreen(
                vm, authViewModel,
                driverViewModel = driverViewModel,
                navController = navController
            )
        }
    }
}