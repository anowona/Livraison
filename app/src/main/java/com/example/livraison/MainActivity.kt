package com.example.livraison

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.livraison.navigation.NavGraph
import com.example.livraison.ui.screens.BottomNavigationBar
import com.example.livraison.ui.theme.LivraisonTheme
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LivraisonTheme {
                val vm: MainViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                val navController = rememberNavController()

                val uiState by authViewModel.uiState.collectAsState()

                Scaffold(
                    topBar = {
                        val title = if (uiState.user?.email != null) {
                            "Welcome, ${uiState.user?.email}"
                        } else {
                            "Delivery App"
                        }
                        androidx.compose.material3.TopAppBar(title = { Text(title) })
                    },
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(vm = vm, navController = navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LivraisonTheme {
        val vm = MainViewModel()
        val navController = rememberNavController()

        Scaffold(
            topBar = {
                androidx.compose.material3.TopAppBar(
                    title = { Text("Delivery App") }
                )
            },
            bottomBar = { BottomNavigationBar(navController = navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavGraph(vm = vm, navController = navController)
            }
        }
    }
}
