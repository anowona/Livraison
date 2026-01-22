package com.example.livraison.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.livraison.ui.screens.*
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(vm: MainViewModel, navController: NavHostController) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUserId = authViewModel.currentUserId

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(authViewModel, navController)
        }
        composable("register") {
            RegisterScreen(authViewModel, navController)
        }
        composable("role_selection") {
            RoleSelectionScreen(navController)
        }
        composable("home") {
            HomeScreen(vm) {
                navController.navigate("cart")
            }
        }
        composable("cart") {
            CartScreen(vm, authViewModel, navController)
        }
        composable("tracking") {
            if (currentUserId.isNotBlank()) {
                OrderTrackingScreen(vm, navController, currentUserId)
            }
        }
        composable("map") {
            OSMMapScreen()
        }
    }
}
