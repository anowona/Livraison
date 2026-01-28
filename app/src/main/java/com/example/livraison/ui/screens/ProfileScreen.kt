package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
        topBar = { TopAppBar(title = { Text("My Profile") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Logged in as:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = user?.email ?: "No email found",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // Edit Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { authViewModel.updateProfile(displayName) },
                        enabled = displayName != user?.displayName,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Save Profile")
                    }
                }
            }
            
            // Actions
            Spacer(modifier = Modifier.weight(1f))

            // Order History Button
            Button(
                onClick = { navController.navigate("order_history") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.History, contentDescription = "Order History", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Order History")
            }

            // Logout Button
            TextButton(
                onClick = { authViewModel.logout() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}
