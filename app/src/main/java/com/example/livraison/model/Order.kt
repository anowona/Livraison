package com.example.livraison.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    var driverId: String? = null, // Added driverId
    val products: List<Product> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.CREATED,
    @ServerTimestamp
    val createdAt: Date? = null,
    val driverLocation: GeoPoint? = null // Added for location tracking
) {
    // Add a custom deserializer for the 'createdAt' field
    @Suppress("UNCHECKED_CAST")
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "driverId" to driverId,
            "products" to products.map { it.toMap() },
            "total" to total,
            "status" to status.name,
            "createdAt" to createdAt,
            "driverLocation" to driverLocation
        )
    }

    companion object {
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): Order {
            val createdAt = when (val ts = map["createdAt"]) {
                is Timestamp -> ts.toDate()
                is Long -> Date(ts)
                else -> null
            }
            return Order(
                id = map["id"] as String,
                userId = map["userId"] as String,
                driverId = map["driverId"] as? String,
                products = (map["products"] as? List<Map<String, Any>>)?.map { Product.fromMap(it) } ?: emptyList(),
                total = map["total"] as Double,
                status = OrderStatus.valueOf(map["status"] as String),
                createdAt = createdAt,
                driverLocation = map["driverLocation"] as? GeoPoint
            )
        }
    }
    constructor() : this("", "", null, emptyList(), 0.0, OrderStatus.CREATED, null, null)
}

private fun Product.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "name" to name,
        "price" to price
    )
}
