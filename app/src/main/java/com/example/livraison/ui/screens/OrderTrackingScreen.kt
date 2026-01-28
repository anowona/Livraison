package com.example.livraison.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavHostController
import com.example.livraison.R
import com.example.livraison.model.OrderStatus
import com.example.livraison.network.RetrofitInstance
import com.example.livraison.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    vm: MainViewModel,
    navController: NavHostController,
    userId: String
) {
    val currentOrder by vm.currentOrder.collectAsState()
    var routeGeometry by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Get colors from the theme here, within the @Composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    LaunchedEffect(userId) {
        vm.observeCurrentOrder(userId)
    }

    // Effect to fetch the route when locations change
    LaunchedEffect(currentOrder?.driverLocation, currentOrder?.address?.geoPoint) {
        val driverLoc = currentOrder?.driverLocation
        val customerLoc = currentOrder?.address?.geoPoint
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
        topBar = { TopAppBar(title = { Text("Track Your Order") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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

                Card(modifier = Modifier.fillMaxWidth().height(450.dp).padding(horizontal = 16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
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


                            // 1. Add Customer Address Marker
                            order.address?.geoPoint?.let {
                                val geoPoint = GeoPoint(it.latitude, it.longitude)
                                val customerMarkerIcon = defaultMarker.mutate()
                                DrawableCompat.setTint(customerMarkerIcon, primaryColor.toArgb())
                                val customerMarker = Marker(mapView).apply {
                                    position = geoPoint
                                    title = "Delivery Address"
                                    icon = customerMarkerIcon
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mapView.overlays.add(customerMarker)
                                points.add(geoPoint)
                            }

                            // 2. Add Driver Location Marker
                            order.driverLocation?.let {
                                val geoPoint = GeoPoint(it.latitude, it.longitude)
                                val driverMarkerIcon = defaultMarker.mutate()
                                DrawableCompat.setTint(driverMarkerIcon, secondaryColor.toArgb())
                                val driverMarker = Marker(mapView).apply {
                                    position = geoPoint
                                    title = "Driver"
                                    icon = driverMarkerIcon
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mapView.overlays.add(driverMarker)
                                points.add(geoPoint)
                            }

                            // 3. Draw the Route
                            routeGeometry?.let {
                                val routePoints = decodePolyline(it, 5) // Use the helper function
                                val polyline = Polyline().apply {
                                    setPoints(routePoints)
                                    outlinePaint.color = Color(0xFF4A80F5).toArgb()
                                    outlinePaint.strokeWidth = 12f
                                }
                                mapView.overlays.add(0, polyline)
                            }

                            // 4. Auto-zoom
                            if (points.size > 1) {
                                mapView.post {
                                    val boundingBox = BoundingBox.fromGeoPoints(points)
                                    mapView.zoomToBoundingBox(boundingBox, true, 150)
                                }
                            } else if (points.isNotEmpty()){
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

/**
 * Decodes a polyline string into a list of GeoPoints.
 * This is a self-contained version of the logic from osmdroid's Polyline class.
 */
private fun decodePolyline(encoded: String, precision: Int): List<GeoPoint> {
    val poly = ArrayList<GeoPoint>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    val factor = Math.pow(10.0, precision.toDouble())

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = GeoPoint(lat.toDouble() / factor, lng.toDouble() / factor)
        poly.add(p)
    }
    return poly
}
