package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.example.livraison.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun OrderTrackingScreen(
    vm: MainViewModel,
    navController: NavHostController,
    userId: String
) {
    val scope = rememberCoroutineScope()
    var currentOrder by vm.currentOrder.collectAsState()

    LaunchedEffect(userId) {
        vm.observeCurrentOrder(userId) { order ->
            currentOrder = order
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text("Order Tracking", fontSize = 22.sp)
        Spacer(modifier = Modifier.height(16.dp))

        if (currentOrder == null) {
            Text("No active order")
        } else {
            Text("Order ID: ${currentOrder!!.id}")
            Text("Status: ${currentOrder!!.status}")

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                scope.launch { vm.updateOrderStatus(currentOrder!!.id, OrderStatus.PREPARING) }
            }) { Text("Preparing") }

            Button(onClick = {
                scope.launch { vm.updateOrderStatus(currentOrder!!.id, OrderStatus.ON_THE_WAY) }
            }) { Text("On the way") }

            Button(onClick = {
                scope.launch { vm.updateOrderStatus(currentOrder!!.id, OrderStatus.DELIVERED) }
            }) { Text("Delivered") }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("map") }) {
                Text("Voir le livreur sur la carte")
            }
        }
    }
}
