package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel

@Composable
fun OrderHistoryScreen(vm: MainViewModel, authViewModel: AuthViewModel) {
    val orderHistory by vm.orderHistory.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val userId = authState.user?.uid

    LaunchedEffect(userId) {
        if (userId != null) {
            vm.loadOrderHistory(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Order History")
        if (orderHistory.isEmpty()) {
            Text("No past orders found.")
        } else {
            LazyColumn {
                items(orderHistory) { order ->
                    Text("Order ID: ${order.id}")
                    Text("Total: ${order.total}")
                    Text("Status: ${order.status}")
                }
            }
        }
    }
}
