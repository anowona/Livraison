package com.example.livraison.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.livraison.model.OrderStatus
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.DriverViewModel

@Composable
fun DriverDashboardScreen(driverViewModel: DriverViewModel, authViewModel: AuthViewModel) {

    val availableOrders by driverViewModel.availableOrders.collectAsState()
    val myOrders by driverViewModel.myOrders.collectAsState()
    val currentUser = authViewModel.uiState.collectAsState().value.user

    // 3. IMPORTANT: REMOVE this LaunchedEffect.
    // We will move this logic to a higher level.
    /*
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            Log.d("DriverDashboard", "User is not null, fetching orders for UID: ${currentUser.uid}")
            driverViewModel.fetchOrders(currentUser.uid)
        } else {
            Log.w("DriverDashboard", "User is null. Cannot fetch orders.")
        }
    }
    */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Active Orders")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(myOrders) { order ->
                Text("Order ID: ${order.id}")
                Text("Status: ${order.status}")
                // Add buttons to update status
                if (order.status == OrderStatus.PREPARING) {
                    Button(onClick = { driverViewModel.updateOrderStatus(order, OrderStatus.ON_THE_WAY) }) {
                        Text("On the Way")
                    }
                } else if (order.status == OrderStatus.ON_THE_WAY) {
                    Button(onClick = { driverViewModel.updateOrderStatus(order, OrderStatus.DELIVERED) }) {
                        Text("Delivered")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Available Orders")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(availableOrders) { order ->
                Text("Order ID: ${order.id}")
                Text("Total: ${order.total}")
                Button(onClick = { currentUser?.let { driverViewModel.acceptOrder(order, it.uid) } }) {
                    Text("Accept Order")
                }
            }
        }
    }
}
