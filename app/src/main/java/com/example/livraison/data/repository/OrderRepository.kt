package com.example.livraison.data.repository

import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersRef = db.collection("orders")

    // --- Order Creation ---
    suspend fun createOrder(order: Order): String {
        val docRef = ordersRef.add(order).await()
        return docRef.id
    }

    // --- Order Updates ---
    suspend fun acceptOrder(orderId: String, driverId: String) {
        ordersRef.document(orderId).update(
            mapOf("driverId" to driverId, "status" to OrderStatus.PREPARING.name)
        ).await()
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        ordersRef.document(orderId).update("status", newStatus.name).await()
    }

    suspend fun updateDriverLocation(orderId: String, location: GeoPoint) {
        ordersRef.document(orderId).update("driverLocation", location).await()
    }

    // --- Real-time Data Flows ---

    fun getOrderByIdFlow(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersRef.document(orderId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val order = snapshot?.toObject(Order::class.java)?.copy(id = snapshot.id)
            trySend(order)
        }
        awaitClose { listener.remove() }
    }

    fun getAvailableOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = ordersRef.whereEqualTo("status", OrderStatus.CREATED.name)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val orders = snapshots?.documents?.mapNotNull {
                    it.toObject(Order::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getActiveOrdersForDriverFlow(driverId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersRef.whereEqualTo("driverId", driverId)
            .whereIn("status", listOf(OrderStatus.PREPARING.name, OrderStatus.ON_THE_WAY.name))
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val orders = snapshots?.documents?.mapNotNull {
                    it.toObject(Order::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersByUserFlow(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersRef.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val orders = snapshots?.documents?.mapNotNull {
                    it.toObject(Order::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getOrdersByDriverFlow(driverId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersRef.whereEqualTo("driverId", driverId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val orders = snapshots?.documents?.mapNotNull {
                    it.toObject(Order::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }
}