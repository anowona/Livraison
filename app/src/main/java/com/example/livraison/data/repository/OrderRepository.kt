package com.example.livraison.data.repository

import android.util.Log
import com.example.livraison.model.Order
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
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
        Log.d("OrderRepo", "Fetching orders for userId: $userId")
        val snapshot = ordersRef
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING) // <-- Added sorting
            .get()
            .await()

        Log.d("OrderRepo", "Found ${snapshot.documents.size} documents.")

        return snapshot.documents.mapNotNull { doc ->
            Log.d("OrderRepo", "Document data: ${doc.data}")
            try {
                val order = doc.toObject(Order::class.java)
                if (order == null) {
                    Log.w("OrderRepo", "Document ${doc.id} could not be converted to Order object.")
                    null
                } else {
                    val finalOrder = order.copy(id = doc.id)
                    Log.d("OrderRepo", "Successfully mapped document ${doc.id} to order: $finalOrder")
                    finalOrder
                }
            } catch (e: Exception) {
                Log.e("OrderRepo", "Error converting document ${doc.id}", e)
                null
            }
        }
    }
}
