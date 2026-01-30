package com.example.livraison.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.R
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
    driverViewModel: DriverViewModel,
    navController: NavHostController // Added NavController
) {
    val authState by authViewModel.uiState.collectAsState()
    val user = authState.user
    val userRole = authState.role

    val roleLivreur = stringResource(id = R.string.role_livreur)
    val roleClient = stringResource(id = R.string.role_client)
    val trackingRoute = "tracking?orderId="
    val driverMapRoute = "driver_map/"

    val orderHistory: List<Order> by if (userRole == roleLivreur) {
        driverViewModel.driverOrderHistory.collectAsState()
    } else {
        mainViewModel.orderHistory.collectAsState()
    }

    LaunchedEffect(user, userRole) {
        if (user != null) {
            if (userRole == roleLivreur) {
                driverViewModel.loadDriverOrderHistory(user.uid)
            } else {
                mainViewModel.loadOrderHistory(user.uid)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.order_history_title)) }) }
    ) { padding ->
        if (orderHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(id = R.string.no_past_orders), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orderHistory) { order ->
                    OrderHistoryCard(order = order, onClick = {
                        if (userRole == roleClient) {
                            navController.navigate(trackingRoute + order.id)
                        } else if (userRole == roleLivreur) {
                            navController.navigate(driverMapRoute + order.id)
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order, onClick: () -> Unit) {
    val dateFormatter = SimpleDateFormat(stringResource(id = R.string.date_format), Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.order_number_formatted, order.id.take(6)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.order_total_formatted, order.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.order_status_formatted, order.status),
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
