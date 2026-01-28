package com.example.livraison.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.livraison.model.Category
import com.example.livraison.model.Product
import com.example.livraison.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    goToCart: () -> Unit
) {
    val categories by vm.categories.collectAsState()
    val cart by vm.cart.collectAsState()

    LaunchedEffect(Unit) { vm.loadProducts() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Munchies") }) },
        floatingActionButton = {
            if (cart.isNotEmpty()) {
                BadgedBox(badge = { Badge { Text(cart.size.toString()) } }) {
                    FloatingActionButton(onClick = goToCart) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Shopping Cart")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            categories.forEach { category ->
                stickyHeader {
                    CategoryHeader(name = category.name)
                }
                items(category.products) { product ->
                    ProductCard(
                        product = product,
                        onAddToCart = { vm.addToCart(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(name: String) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ProductCard(product: Product, onAddToCart: (Product) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column {
            SubcomposeAsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Error, contentDescription = "Error loading image", tint = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium)
                    Text("${product.price} €", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onAddToCart(product) }) {
                    Text("Add")
                }
            }
        }
    }
}
