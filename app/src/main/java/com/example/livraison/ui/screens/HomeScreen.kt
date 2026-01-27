package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.livraison.model.Product
import com.example.livraison.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    vm: MainViewModel,
    goToCart: () -> Unit
) {
    val products by vm.products.collectAsState()

    LaunchedEffect(Unit) { vm.loadProducts() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text("${product.price} €", color = MaterialTheme.colorScheme.primary)
                    }

                    Button(onClick = { vm.addToCart(product) }) { Text("Add") }
                }
            }
        }
    }
}
