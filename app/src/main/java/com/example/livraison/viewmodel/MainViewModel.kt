package com.example.livraison.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livraison.data.repository.OrderRepository
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.example.livraison.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = OrderRepository()
    private val db = FirebaseFirestore.getInstance()

    // Cart
    private val _cart = MutableStateFlow<List<Product>>(emptyList())
    val cart: StateFlow<List<Product>> = _cart

    // Products
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    // Current Order
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder

    private var orderListener: ListenerRegistration? = null

    // Order History
    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory

    // -------------------------
    // Load products
    // -------------------------
    fun loadProducts() {
        viewModelScope.launch {
            _products.value = listOf(
                Product(1, "Burger", 5.0),
                Product(2, "Pizza", 7.5),
                Product(3, "Soda", 2.0)
            )
        }
    }

    // -------------------------
    // Cart operations
    // -------------------------
    fun addToCart(product: Product) {
        _cart.value = _cart.value + product
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // -------------------------
    // Create order
    // -------------------------
    fun createOrder(
        userId: String,
        total: Double,
        onSuccess: (orderId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val order = Order(
                    userId = userId,
                    products = _cart.value,
                    total = total,
                    status = OrderStatus.CREATED
                )
                val orderId = repository.createOrder(order)
                clearCart()
                observeCurrentOrder(userId) // auto-start tracking
                onSuccess(orderId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create order")
            }
        }
    }


    // -------------------------
    // Observe current order by user
    // -------------------------
    fun observeCurrentOrder(userId: String) {
        try {
            orderListener?.remove()
            orderListener = db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereIn(
                    "status",
                    listOf(
                        OrderStatus.CREATED.name,
                        OrderStatus.PREPARING.name,
                        OrderStatus.ON_THE_WAY.name
                    )
                )
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w("MainViewModel", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    val doc = snapshots?.documents?.firstOrNull()
                    _currentOrder.value = doc?.let { Order.fromMap(it.data!!).copy(id = it.id) }
                }
        } catch (e: Exception) {
            println("Error observing current order: ${e.message}")
            Log.e("MainViewModel", "Error observing current order", e)
        }


    }

    // -------------------------
    // Load order history
    // -------------------------
    fun loadOrderHistory(userId: String) {
        viewModelScope.launch {
            repository.getOrdersByUserFlow(userId)
                .catch { e ->
                    Log.e("MainViewModel", "Error loading order history", e)
                }
                .collect { orders ->
                    _orderHistory.value = orders
                }
        }
    }

    // -------------------------
    // Update order status
    // -------------------------
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        db.collection("orders")
            .document(orderId)
            .update("status", status.name)
    }

    override fun onCleared() {
        orderListener?.remove()
        super.onCleared()
    }
}
