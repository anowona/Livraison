package com.example.livraison.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object LocationUtils {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocation(context: Context): Flow<GeoPoint?> = callbackFlow {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (!hasLocationPermission(context)) {
            Log.w("LocationUtils", "Location permission not granted.")
            trySend(null)
            close()
            return@callbackFlow
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    Log.d("LocationUtils", "New location received: Lat=${location.latitude}, Lon=${location.longitude}")
                    trySend(GeoPoint(location.latitude, location.longitude))
                } else {
                    Log.w("LocationUtils", "Location result was null.")
                }
            }
        }

        Log.d("LocationUtils", "Requesting location updates...")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        awaitClose { 
            Log.d("LocationUtils", "Stopping location updates.")
            fusedLocationClient.removeLocationUpdates(locationCallback) 
        }
    }
}
