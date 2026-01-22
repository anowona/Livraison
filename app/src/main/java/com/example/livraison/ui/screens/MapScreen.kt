package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint

@Composable
fun OSMMapScreen() {
    var courierPosition by remember { mutableStateOf(GeoPoint(48.8566, 2.3522)) }
    val clientPosition = GeoPoint(48.8600, 2.3300)

    LaunchedEffect(Unit) {
        val steps = 20
        val latStep = (clientPosition.latitude - courierPosition.latitude) / steps
        val lonStep = (clientPosition.longitude - courierPosition.longitude) / steps
        repeat(steps) {
            courierPosition = GeoPoint(
                courierPosition.latitude + latStep,
                courierPosition.longitude + lonStep
            )
            kotlinx.coroutines.delay(500)
        }
    }

    AndroidView(factory = { context ->
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setBuiltInZoomControls(true)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(courierPosition)
        }
    }, update = { mapView ->
        mapView.controller.setCenter(courierPosition) // update map center dynamically
    }, modifier = Modifier.fillMaxSize())
}
