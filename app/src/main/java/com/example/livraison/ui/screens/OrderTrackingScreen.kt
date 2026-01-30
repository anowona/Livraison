package com.example.livraison.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.NavHostController
import com.example.livraison.R
import com.example.livraison.model.OrderStatus
import com.example.livraison.network.RetrofitInstance
import com.example.livraison.utils.decodePolyline // Use the shared utility function
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
    userId: String,
    orderId: String? // Added optional orderId
) {
    val currentOrder by vm.currentOrder.collectAsState()
    var routeGeometry by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    LaunchedEffect(userId, orderId) {
        if (orderId != null) {
            vm.observeOrderById(orderId)
        } else {
            vm.observeCurrentOrder(userId)
        }
    }

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
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.track_your_order)) }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (currentOrder == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.loading_order), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                val order = currentOrder!!
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(id = R.string.order_number_formatted, order.id.take(6)),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OrderStatusIndicator(status = order.status)
                }

                // Only show the map for active orders
                if (order.status == OrderStatus.DELIVERED || order.status == OrderStatus.CANCELED) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(id = R.string.order_completed), style = MaterialTheme.typography.bodyLarge)
                    }
                } else if (order.driverLocation != null || order.address?.geoPoint != null) {
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
                                val defaultMarker = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)!!

                                order.address?.geoPoint?.let {
                                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                                    val customerMarkerIcon = defaultMarker.mutate()
                                    DrawableCompat.setTint(customerMarkerIcon, primaryColor.toArgb())
                                    Marker(mapView).apply {
                                        position = geoPoint
                                        title = context.getString(R.string.delivery_address)
                                        icon = customerMarkerIcon
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        mapView.overlays.add(this)
                                    }
                                    points.add(geoPoint)
                                }

                                order.driverLocation?.let {
                                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                                    val driverMarkerIcon = defaultMarker.mutate()
                                    DrawableCompat.setTint(driverMarkerIcon, secondaryColor.toArgb())
                                    Marker(mapView).apply {
                                        position = geoPoint
                                        title = context.getString(R.string.driver)
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
                                } else if (points.isNotEmpty()){
                                    mapView.controller.setZoom(16.0)
                                    mapView.controller.setCenter(points[0])
                                }
                                mapView.invalidate()
                            }
                        )
                    }
                } else {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(id = R.string.waiting_for_driver_location), style = MaterialTheme.typography.titleMedium)
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

    val animatedProgress by animateFloatAsState(targetValue = progress, label = stringResource(id = R.string.order_progress))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(id = R.string.order_status_formatted, status),
            style = MaterialTheme.typography.titleMedium
        )
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
