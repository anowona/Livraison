package com.example.livraison.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livraison.data.repository.OrderRepository
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.example.livraison.utils.LocationUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DriverViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val ordersRef = db.collection("orders")
    private val repository = OrderRepository()

    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders

    private val _myOrders = MutableStateFlow<List<Order>>(emptyList())
    val myOrders: StateFlow<List<Order>> = _myOrders

    private val _driverOrderHistory = MutableStateFlow<List<Order>>(emptyList())
    val driverOrderHistory: StateFlow<List<Order>> = _driverOrderHistory

    private var locationUpdateJob: Job? = null

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

    fun loadDriverOrderHistory(driverId: String) {
        viewModelScope.launch {
            repository.getOrdersByDriverFlow(driverId)
                .catch { e ->
                    Log.e("DriverViewModel", "Error loading driver order history", e)
                }
                .collect { orders ->
                    _driverOrderHistory.value = orders
                }
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
        if (newStatus == OrderStatus.DELIVERED) {
            stopLocationUpdates()
        }
    }

    fun startLocationUpdates(context: Context, orderId: String) {
        stopLocationUpdates() // Stop any previous updates
        locationUpdateJob = viewModelScope.launch {
            LocationUtils.getCurrentLocation(context)
                .catch { e -> Log.e("DriverViewModel", "Error getting location", e) }
                .collect { location ->
                    if (location != null) {
                        updateDriverLocation(orderId, location)
                    }
                }
        }
    }

    fun stopLocationUpdates() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
    }

    private fun updateDriverLocation(orderId: String, location: GeoPoint) {
        ordersRef.document(orderId)
            .update("driverLocation", location)
            .addOnSuccessListener { Log.d("DriverViewModel", "Driver location updated for order $orderId") }
            .addOnFailureListener { e -> Log.e("DriverViewModel", "Error updating driver location", e) }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
