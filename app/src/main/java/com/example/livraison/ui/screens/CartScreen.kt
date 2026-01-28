package com.example.livraison.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.livraison.model.Product
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    onCheckout: () -> Unit
) {
    val cart by mainViewModel.cart.collectAsState()
    val currentUserId = authViewModel.uiState.collectAsState().value.user?.uid
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) { // Use Surface for a clean container
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total: ${cart.sumOf { it.price }} €", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val uid = currentUserId ?: ""
                                if (uid.isBlank()) {
                                    Log.e("CartScreen", "User not logged in, cannot place order.")
                                    scope.launch { snackbarHostState.showSnackbar("You must be logged in to place an order.") }
                                    return@Button
                                }

                                mainViewModel.createOrder(
                                    userId = uid,
                                    total = cart.sumOf { it.price },
                                    onSuccess = { onCheckout() },
                                    onError = { errorMsg ->
                                        Log.e("CartScreen", "Failed to create order: $errorMsg")
                                        scope.launch { snackbarHostState.showSnackbar("Error: $errorMsg") }
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Commander")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (cart.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(cart) { product ->
                        ProductCartItem(product)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCartItem(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("${product.price} €", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
