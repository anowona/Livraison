package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.livraison.model.OrderStatus
import com.example.livraison.network.RetrofitInstance
import com.example.livraison.utils.decodePolyline
import com.example.livraison.viewmodel.DriverViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.library.R
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverMapScreen(
    navController: NavHostController,
    orderId: String,
    driverViewModel: DriverViewModel = viewModel()
) {
    val order by driverViewModel.selectedOrder.collectAsState()
    var routeGeometry by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    LaunchedEffect(orderId) {
        driverViewModel.fetchOrderById(orderId)
    }

    LaunchedEffect(order?.driverLocation, order?.address?.geoPoint) {
        val driverLoc = order?.driverLocation
        val customerLoc = order?.address?.geoPoint
        if (driverLoc != null && customerLoc != null) {
            scope.launch {
                try {
                    val coordinates = "${driverLoc.longitude},${driverLoc.latitude};${customerLoc.longitude},${customerLoc.latitude}"
                    val response = RetrofitInstance.routingApi.getRoute(coordinates)
                    if (response.routes.isNotEmpty()) {
                        routeGeometry = response.routes[0].geometry
                    }
                } catch (e: Exception) {
                    // Handle exceptions
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Delivery Route") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (order == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading order details...")
                }
            } else {
                val currentOrder = order!!
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Delivering to:", style = MaterialTheme.typography.titleMedium)
                    Text(currentOrder.address?.street ?: "Address not available", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (currentOrder.status == OrderStatus.DELIVERED || currentOrder.status == OrderStatus.CANCELED) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("This order has been completed.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    Card(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
                                MapView(context).apply { setMultiTouchControls(true) }
                            },
                            update = { mapView ->
                                mapView.overlays.clear()
                                val points = mutableListOf<GeoPoint>()
                                val context = mapView.context
                                val defaultMarker = ContextCompat.getDrawable(context, R.drawable.marker_default)!!

                                currentOrder.address?.geoPoint?.let {
                                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                                    val customerMarkerIcon = defaultMarker.mutate()
                                    DrawableCompat.setTint(customerMarkerIcon, primaryColor.toArgb())
                                    Marker(mapView).apply {
                                        position = geoPoint
                                        title = "Delivery Address"
                                        icon = customerMarkerIcon
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        mapView.overlays.add(this)
                                    }
                                    points.add(geoPoint)
                                }

                                currentOrder.driverLocation?.let {
                                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                                    val driverMarkerIcon = defaultMarker.mutate()
                                    DrawableCompat.setTint(driverMarkerIcon, secondaryColor.toArgb())
                                    Marker(mapView).apply {
                                        position = geoPoint
                                        title = "Your Location"
                                        icon = driverMarkerIcon
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        mapView.overlays.add(this)
                                    }
                                    points.add(geoPoint)
                                }

                                routeGeometry?.let {
                                    val routePoints = decodePolyline(it, 5)
                                    val polyline = Polyline().apply {
                                        setPoints(routePoints)
                                        outlinePaint.color = Color(0xFF4A80F5).toArgb()
                                        outlinePaint.strokeWidth = 12f
                                    }
                                    mapView.overlays.add(0, polyline)
                                }

                                if (points.size > 1) {
                                    mapView.post { mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 150) }
                                } else if (points.isNotEmpty()) {
                                    mapView.controller.setZoom(16.0)
                                    mapView.controller.setCenter(points[0])
                                }
                                mapView.invalidate()
                            }
                        )
                    }
                }
            }
        }
    }
}