package com.example.livraison

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.livraison.ui.screens.ClientMainScreen
import com.example.livraison.ui.screens.DriverMainScreen
import com.example.livraison.ui.screens.LoginScreen
import com.example.livraison.ui.screens.RoleSelectionScreen
import com.example.livraison.ui.theme.LivraisonTheme
import com.example.livraison.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LivraisonTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()

                // This LaunchedEffect is the new centralized router.
                // It observes the authentication state and navigates accordingly.
                LaunchedEffect(authState.user, authState.role, authState.isRoleLoading) {
                    if (!authState.isRoleLoading) {
                        val destination = when {
                            authState.user == null -> "login"
                            authState.role == "livreur" -> "driver_main"
                            authState.role == "client" -> "client_main"
                            else -> "role_selection" // User is logged in but has no role
                        }
                        
                        // Avoid navigating if we are already at the correct destination
                        if (navController.currentDestination?.route != destination) {
                            navController.navigate(destination) {
                                // Clear the entire back stack
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                NavHost(navController, startDestination = "loading") {
                    composable("loading") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    composable("login") { LoginScreen(authViewModel, navController) }
                    composable("role_selection") { RoleSelectionScreen(navController) }
                    composable("client_main") { ClientMainScreen() }
                    composable("driver_main") { DriverMainScreen() }
                }
            }
        }
    }
}
