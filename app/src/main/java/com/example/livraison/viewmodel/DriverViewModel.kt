package com.example.livraison.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livraison.data.repository.OrderRepository
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.example.livraison.utils.LocationUtils
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DriverViewModel : ViewModel() {

    private val repository = OrderRepository()

    private val _availableOrders = MutableStateFlow<List<Order>>(emptyList())
    val availableOrders: StateFlow<List<Order>> = _availableOrders

    private val _myOrders = MutableStateFlow<List<Order>>(emptyList())
    val myOrders: StateFlow<List<Order>> = _myOrders

    private val _driverOrderHistory = MutableStateFlow<List<Order>>(emptyList())
    val driverOrderHistory: StateFlow<List<Order>> = _driverOrderHistory

    private val _selectedOrder = MutableStateFlow<Order?>(null) // To hold the order for the map screen
    val selectedOrder: StateFlow<Order?> = _selectedOrder

    private var locationUpdateJob: Job? = null

    fun fetchOrders(driverId: String) {
        viewModelScope.launch {
            repository.getAvailableOrdersFlow()
                .catch { e -> Log.e("DriverViewModel", "Error fetching available orders", e) }
                .collect { orders -> _availableOrders.value = orders }
        }
        viewModelScope.launch {
            repository.getActiveOrdersForDriverFlow(driverId)
                .catch { e -> Log.e("DriverViewModel", "Error fetching active orders", e) }
                .collect { orders -> _myOrders.value = orders }
        }
    }
    
    fun fetchOrderById(orderId: String) {
        viewModelScope.launch {
             repository.getOrderByIdFlow(orderId)
                .catch { e -> Log.e("DriverViewModel", "Error fetching order by id", e) }
                .collect { order -> _selectedOrder.value = order }
        }
    }

    fun loadDriverOrderHistory(driverId: String) {
        viewModelScope.launch {
            repository.getOrdersByDriverFlow(driverId)
                .catch { e -> Log.e("DriverViewModel", "Error loading driver order history", e) }
                .collect { orders -> _driverOrderHistory.value = orders }
        }
    }

    fun acceptOrder(order: Order, driverId: String) {
        viewModelScope.launch {
            try {
                repository.acceptOrder(order.id, driverId)
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error accepting order", e)
            }
        }
    }

    fun updateOrderStatus(order: Order, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(order.id, newStatus)
                if (newStatus == OrderStatus.DELIVERED) {
                    stopLocationUpdates()
                }
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error updating order status", e)
            }
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
        viewModelScope.launch {
            try {
                repository.updateDriverLocation(orderId, location)
            } catch (e: Exception) {
                Log.e("DriverViewModel", "Error updating driver location", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
