package com.example.livraison.data.repository

import com.example.livraison.model.Order
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = Firebase.firestore
    private val ordersRef = db.collection("orders")

    suspend fun createOrder(order: Order): String {
        val docRef = ordersRef.add(order).await()
        return docRef.id
    }

    suspend fun getOrdersByUser(userId: String): List<Order> {
        return ordersRef
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }
    }
}
