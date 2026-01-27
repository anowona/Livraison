package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val snackbarHostState = remember { SnackbarHostState() }

    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }

    LaunchedEffect(authState.profileUpdateMessage) {
        authState.profileUpdateMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.dismissProfileMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You are logged in as: ${user?.email ?: ""}")
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { authViewModel.updateProfile(displayName) },
                enabled = displayName != user?.displayName
            ) {
                Text("Save Profile")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { navController.navigate("order_history") }) {
                Text("Order History")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { authViewModel.logout() }) {
                Text("Logout")
            }
        }
    }
}
