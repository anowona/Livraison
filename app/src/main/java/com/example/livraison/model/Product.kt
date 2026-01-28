package com.example.livraison.model

data class Product(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "" // Added for displaying product images
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Product {
            return Product(
                id = (map["id"] as Long).toInt(),
                name = map["name"] as String,
                price = map["price"] as Double,
                imageUrl = map["imageUrl"] as? String ?: "" // Handle potential nulls
            )
        }
    }
}
