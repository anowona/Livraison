package com.example.livraison.ui.screens

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.R
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.example.livraison.utils.LocationUtils
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.DriverViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    driverViewModel: DriverViewModel, 
    authViewModel: AuthViewModel,
    navController: NavHostController
) {

    val context = LocalContext.current
    val availableOrders by driverViewModel.availableOrders.collectAsState()
    val myOrders by driverViewModel.myOrders.collectAsState()
    val currentUser = authViewModel.uiState.collectAsState().value.user

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Log.d("DriverDashboard", context.getString(R.string.location_permission_granted))
            } else {
                Log.w("DriverDashboard", context.getString(R.string.location_permission_denied))
            }
        }
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.driver_dashboard_title)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Section for My Active Orders
            SectionTitle(stringResource(id = R.string.my_active_orders))
            if (myOrders.isEmpty()) {
                EmptyState(message = stringResource(id = R.string.no_active_orders), modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(myOrders) { order ->
                        MyOrderCard(
                            order = order, 
                            onUpdateClick = {
                                if (order.status == OrderStatus.PREPARING) {
                                    if (LocationUtils.hasLocationPermission(context)) {
                                        driverViewModel.updateOrderStatus(order, OrderStatus.ON_THE_WAY)
                                        driverViewModel.startLocationUpdates(context, order.id)
                                    } else {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                } else {
                                    driverViewModel.updateOrderStatus(order, OrderStatus.DELIVERED)
                                }
                            },
                            onCardClick = { 
                                // Only navigate if the order is actually on the way
                                if (order.status == OrderStatus.ON_THE_WAY) {
                                    navController.navigate("driver_map/${order.id}") 
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section for Available Orders
            SectionTitle(stringResource(id = R.string.available_orders))
            if (availableOrders.isEmpty()) {
                EmptyState(message = stringResource(id = R.string.no_available_orders), modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(availableOrders) { order ->
                        AvailableOrderCard(order = order, onAcceptClick = {
                            currentUser?.let { driverViewModel.acceptOrder(order, it.uid) }
                        })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MyOrderCard(order: Order, onUpdateClick: () -> Unit, onCardClick: () -> Unit) {
    // The card is only clickable if the order is ON_THE_WAY
    val isCardClickable = order.status == OrderStatus.ON_THE_WAY

    Card(
        elevation = CardDefaults.cardElevation(2.dp), 
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isCardClickable, onClick = onCardClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(id = R.string.order_id, order.id.take(8)), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(id = R.string.order_status_formatted, order.status), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (order.status == OrderStatus.PREPARING) {
                    Button(onClick = onUpdateClick) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.on_the_way))
                    }
                } else if (order.status == OrderStatus.ON_THE_WAY) {
                    Button(onClick = onUpdateClick) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = R.string.delivered))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AvailableOrderCard(order: Order, onAcceptClick: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stringResource(id = R.string.new_order), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(id = R.string.total_euros, order.total), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onAcceptClick) {
                Text(stringResource(id = R.string.accept))
            }
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
