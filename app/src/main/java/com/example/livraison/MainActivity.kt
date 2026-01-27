package com.example.livraison

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.livraison.ui.screens.ClientMainScreen
import com.example.livraison.ui.screens.DriverMainScreen
import com.example.livraison.ui.screens.LoginScreen
import com.example.livraison.ui.screens.RoleSelectionScreen
import com.example.livraison.ui.screens.RoutingScreen
import com.example.livraison.ui.theme.LivraisonTheme
import com.example.livraison.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LivraisonTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()

                NavHost(navController, startDestination = "routing") {
                    composable("routing") { RoutingScreen(navController) }
                    composable("login") { LoginScreen(authViewModel, navController) }
                    composable("role_selection") { RoleSelectionScreen(navController) }
                    composable("client_main") { ClientMainScreen() }
                    composable("driver_main") { DriverMainScreen() }
                }
            }
        }
    }
}
