package com.example.livraison.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.livraison.model.OrderStatus
import com.example.livraison.viewmodel.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    vm: MainViewModel,
    navController: NavHostController,
    userId: String
) {
    val currentOrder by vm.currentOrder.collectAsState()

    LaunchedEffect(userId) {
        vm.observeCurrentOrder(userId)
    }
    LaunchedEffect(currentOrder) {
        Log.d("OrderTracking", "currentOrder = $currentOrder")
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Track Your Order") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (currentOrder == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active order found.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                val order = currentOrder!!
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Order #${order.id.take(6)}...", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OrderStatusIndicator(status = order.status)
                }

                if (order.driverLocation != null) {
                    val driverLocation = order.driverLocation!!
                    val geoPoint = GeoPoint(driverLocation.latitude, driverLocation.longitude)

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
                            MapView(context).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(17.0)
                            }
                        },
                        update = { mapView ->
                            mapView.overlays.clear()
                            val driverMarker = Marker(mapView)
                            driverMarker.position = geoPoint
                            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            mapView.overlays.add(driverMarker)
                            mapView.controller.setCenter(geoPoint)
                            mapView.invalidate()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Waiting for driver...", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusIndicator(status: OrderStatus) {
    val progress = when (status) {
        OrderStatus.CREATED -> 0.2f
        OrderStatus.PREPARING -> 0.5f
        OrderStatus.ON_THE_WAY -> 0.8f
        OrderStatus.DELIVERED -> 1.0f
        OrderStatus.CANCELED -> 0.0f
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "Order Progress")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Status: $status", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.medium)
        )
    }
}
