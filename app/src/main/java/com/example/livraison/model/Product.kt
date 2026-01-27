package com.example.livraison.model

data class Product(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0
) {
    companion object {
        fun fromMap(map: Map<String, Any>): Product {
            return Product(
                id = (map["id"] as Long).toInt(),
                name = map["name"] as String,
                price = map["price"] as Double
            )
        }
    }
}
