package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.livraison.navigation.ClientNavGraph
import com.example.livraison.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientMainScreen() {
    val navController = rememberNavController()
    val vm: MainViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController, userRole = "client") }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ClientNavGraph(vm = vm, navController = navController)
        }
    }
}
