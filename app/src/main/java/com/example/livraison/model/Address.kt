package com.example.livraison.model

import com.google.firebase.firestore.GeoPoint
import java.util.UUID

/**
 * Represents a user's address.
 * Includes a GeoPoint for map integration.
 */
data class Address(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // e.g., "Home", "Work"
    val street: String = "",
    val city: String = "",
    val postalCode: String = "",
    val geoPoint: GeoPoint? = null
) {
    // Empty constructor for Firestore serialization
    constructor() : this(id = UUID.randomUUID().toString())

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "street" to street,
            "city" to city,
            "postalCode" to postalCode,
            "geoPoint" to geoPoint
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Address {
            return Address(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                name = map["name"] as? String ?: "",
                street = map["street"] as? String ?: "",
                city = map["city"] as? String ?: "",
                postalCode = map["postalCode"] as? String ?: "",
                geoPoint = map["geoPoint"] as? GeoPoint
            )
        }
    }
}