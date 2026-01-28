package com.example.livraison.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.livraison.model.Product
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    onCheckout: () -> Unit
) {
    val cart by mainViewModel.cart.collectAsState()
    val currentUserId = authViewModel.currentUid

    Scaffold(
        bottomBar = {
            if (cart.isNotEmpty()) {
                BottomAppBar(modifier = Modifier.padding(16.dp)) {
                    Column {
                        Text("Total: ${cart.sumOf { it.price }} €", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val uid = currentUserId ?: ""
                                if (uid.isBlank()) {
                                    Log.e("CartScreen", "User not logged in, cannot place order.")
                                    return@Button
                                }

                                mainViewModel.createOrder(
                                    userId = uid,
                                    total = cart.sumOf { it.price },
                                    onSuccess = { onCheckout() },
                                    onError = { errorMsg ->
                                        Log.e("CartScreen", "Failed to create order: $errorMsg")
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
