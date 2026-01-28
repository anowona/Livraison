package com.example.livraison.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DriverViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ordersRef = db.collection("orders")

    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders

    private val _myOrders = MutableStateFlow<List<Order>>(emptyList())
    val myOrders: StateFlow<List<Order>> = _myOrders

    fun fetchOrders(driverId: String) {
        // Fetch available orders
        ordersRef.whereEqualTo("status", OrderStatus.CREATED.name)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("DriverViewModel", "Available orders listen failed.", e)
                    return@addSnapshotListener
                }

                val orders = snapshots?.mapNotNull { doc ->
                    try {
                        Order.fromMap(doc.data).copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("DriverViewModel", "Error converting available order", e)
                        null
                    }
                } ?: emptyList()

                _availableOrders.value = orders
                Log.d("DriverViewModel", "Fetched ${orders.size} available orders.")
            }

        // Fetch orders assigned to this driver
        ordersRef.whereEqualTo("driverId", driverId)
            .whereIn("status", listOf(OrderStatus.PREPARING.name, OrderStatus.ON_THE_WAY.name))
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("DriverViewModel", "My orders listen failed.", e)
                    return@addSnapshotListener
                }
                val orders = snapshots?.mapNotNull { doc ->
                    try {
                        Order.fromMap(doc.data).copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("DriverViewModel", "Error converting my order", e)
                        null
                    }
                } ?: emptyList()
                _myOrders.value = orders
            }
    }



    fun acceptOrder(order: Order, driverId: String) {
        ordersRef.document(order.id)
            .update(
                mapOf(
                    "driverId" to driverId,
                    "status" to OrderStatus.PREPARING.name
                )
            )
    }

    fun updateOrderStatus(order: Order, newStatus: OrderStatus) {
        ordersRef.document(order.id).update("status", newStatus.name)
    }
}
