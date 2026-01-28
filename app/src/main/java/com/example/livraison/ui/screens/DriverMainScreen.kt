package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.livraison.navigation.DriverNavGraph
import com.example.livraison.viewmodel.DriverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMainScreen() {
    val navController = rememberNavController()
    val driverViewModel: DriverViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(
            navController = navController, userRole = "livreur",
            driverViewModel = driverViewModel,
            mainViewModel = null
        ) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DriverNavGraph(navController = navController)
        }
    }
}
