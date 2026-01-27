package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.livraison.navigation.DriverNavGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController, userRole = "livreur") }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            DriverNavGraph(navController = navController)
        }
    }
}
