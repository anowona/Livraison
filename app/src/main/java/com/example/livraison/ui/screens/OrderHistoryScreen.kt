package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.livraison.model.Order
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.DriverViewModel
import com.example.livraison.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    driverViewModel: DriverViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val userRole = authState.role

    val orderHistory: List<Order> by if (userRole == "driver") {
        driverViewModel.driverOrderHistory.collectAsState()
    } else {
        mainViewModel.orderHistory.collectAsState()
    }

    LaunchedEffect(user, userRole) {
        if (user != null) {
            if (userRole == "driver") {
                driverViewModel.loadDriverOrderHistory(user.uid)
            } else {
                mainViewModel.loadOrderHistory(user.uid)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Order History") }) }
    ) { padding ->
        if (orderHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No past orders found.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orderHistory) { order ->
                    OrderHistoryCard(order = order)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order) {
    val dateFormatter = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${order.total} €",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Status: ${order.status}",
                style = MaterialTheme.typography.bodyMedium
            )
            order.createdAt?.let {
                Text(
                    text = dateFormatter.format(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
