package com.example.livraison.model


data class Order(
    val id: String = "",
    val userId: String = "",
    val products: List<Product> = emptyList(),
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.CREATED,
    val createdAt: Long = System.currentTimeMillis()
)
