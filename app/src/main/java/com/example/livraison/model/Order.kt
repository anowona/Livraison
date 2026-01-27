package com.example.livraison.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val products: List<Product> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.CREATED,
    @ServerTimestamp
    val createdAt: Date? = null
)
