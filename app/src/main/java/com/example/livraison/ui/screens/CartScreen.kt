package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.model.Product
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel

@Composable
fun CartScreen(
    vm: MainViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    val cart by vm.cart.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Your Cart", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        cart.forEach { product: Product ->
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(product.name)
                    Text("${product.price} €")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Total: ${cart.sumOf { it.price }} €", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val uid = authViewModel.currentUserId
            if (uid.isBlank()) {
                // Show a toast/snackbar instead of crashing
                println("Error: user not logged in")
                return@Button
            }

            vm.createOrder(
                userId = uid,
                total = cart.sumOf { it.price },
                onSuccess = { orderId ->
                    // Start listening to the order for tracking
                    vm.observeOrder(orderId)
                    navController.navigate("tracking")
                },
                onError = { errorMsg ->
                    println("Failed to create order: $errorMsg")
                }
            )
        }) {
            Text("Commander")
        }

    }
}
