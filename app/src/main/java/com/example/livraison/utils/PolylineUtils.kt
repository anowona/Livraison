package com.example.livraison.utils

import org.osmdroid.util.GeoPoint

/**
 * Decodes a polyline string into a list of GeoPoints.
 * This is a self-contained version of the logic from osmdroid's Polyline class.
 */
fun decodePolyline(encoded: String, precision: Int): List<GeoPoint> {
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