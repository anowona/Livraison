package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.model.Address
import com.example.livraison.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val addresses = authState.addresses
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Logged in as:", style = MaterialTheme.typography.labelMedium)
                    Text(user?.email ?: "No email found", style = MaterialTheme.typography.bodyLarge)
                }
            }

            // Addresses Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Addresses", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { showAddAddressDialog = true }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add Address")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (addresses.isEmpty()) {
                        Text("No addresses saved.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column {
                            addresses.forEach { address ->
                                Text(address.name, fontWeight = FontWeight.Bold)
                                Text("${address.street}, ${address.city}, ${address.postalCode}")
                                if (addresses.last() != address) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Actions
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("order_history") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.History, contentDescription = "Order History", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Order History")
            }

            TextButton(onClick = { authViewModel.logout() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }

        if (showAddAddressDialog) {
            AddAddressDialog(
                onDismiss = { showAddAddressDialog = false },
                onAddAddress = { newAddress ->
                    user?.uid?.let { authViewModel.addAddress(context, it, newAddress) }
                    showAddAddressDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddAddressDialog(onDismiss: () -> Unit, onAddAddress: (Address) -> Unit) {
    var name by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Address Name (e.g. Home)") })
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    label = { Text("Postal Code") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val newAddress = Address(name = name, street = street, city = city, postalCode = postalCode)
                onAddAddress(newAddress)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
