package com.example.livraison.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livraison.data.repository.OrderRepository
import com.example.livraison.model.Address
import com.example.livraison.model.Category
import com.example.livraison.model.Order
import com.example.livraison.model.OrderStatus
import com.example.livraison.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = OrderRepository()
    private val db = FirebaseFirestore.getInstance()

    // Cart
    private val _cart = MutableStateFlow<List<Product>>(emptyList())
    val cart: StateFlow<List<Product>> = _cart

    // Original categories list
    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filtered categories based on search
    val filteredCategories: StateFlow<List<Category>> = _searchQuery
        .combine(_categories) { query, categories ->
            if (query.isBlank()) {
                categories
            } else {
                categories.mapNotNull { category ->
                    val filteredProducts = category.products.filter {
                        it.name.contains(query, ignoreCase = true)
                    }
                    if (filteredProducts.isNotEmpty()) {
                        category.copy(products = filteredProducts)
                    } else {
                        null
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Order
    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder

    private var orderListener: ListenerRegistration? = null

    // Order History
    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory

    init {
        loadProducts()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _categories.value = getCategorizedMenu()
        }
    }

    fun addToCart(product: Product) {
        _cart.value = _cart.value + product
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun createOrder(
        userId: String,
        total: Double,
        address: Address,
        onSuccess: (orderId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val order = Order(
                    userId = userId,
                    products = _cart.value,
                    total = total,
                    address = address,
                    status = OrderStatus.CREATED
                )
                val orderId = repository.createOrder(order)
                clearCart()
                observeCurrentOrder(userId)
                onSuccess(orderId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create order")
            }
        }
    }

    fun observeCurrentOrder(userId: String) {
        try {
            orderListener?.remove()
            orderListener = db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereIn("status", listOf(OrderStatus.CREATED.name, OrderStatus.PREPARING.name, OrderStatus.ON_THE_WAY.name))
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
            Log.e("MainViewModel", "Error observing current order", e)
        }
    }

    fun observeOrderById(orderId: String) {
        viewModelScope.launch {
            repository.getOrderByIdFlow(orderId)
                .catch { e -> Log.e("MainViewModel", "Error observing order by id", e) }
                .collect { order -> _currentOrder.value = order }
        }
    }

    fun loadOrderHistory(userId: String) {
        viewModelScope.launch {
            repository.getOrdersByUserFlow(userId)
                .catch { e -> Log.e("MainViewModel", "Error loading order history", e) }
                .collect { orders -> _orderHistory.value = orders }
        }
    }

    override fun onCleared() {
        orderListener?.remove()
        super.onCleared()
    }

    private fun getCategorizedMenu(): List<Category> {
        return listOf(
            Category(
                name = "Burgers",
                products = listOf(
                    Product(1, "Classic Burger", 8.99, "https://images.unsplash.com/photo-1571091718767-18b5b1457add?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1172&q=80"),
                    Product(2, "Cheeseburger", 9.99, "https://images.unsplash.com/photo-1607013251379-e6eecfffe234?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80"),
                    Product(3, "Bacon Burger", 10.99, "https://images.unsplash.com/photo-1551984318-c8a8b13d29b8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80")
                )
            ),
            Category(
                name = "Pizzas",
                products = listOf(
                    Product(4, "Margherita Pizza", 12.50, "https://images.unsplash.com/photo-1594007654729-407eedc4be65?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=728&q=80"),
                    Product(5, "Pepperoni Pizza", 14.00, "https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1176&q=80"),
                    Product(6, "Vegetarian Pizza", 13.00, "https://images.unsplash.com/photo-1513104890138-7c749659a591?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80")
                )
            ),
            Category(
                name = "Desserts",
                products = listOf(
                    Product(7, "Chocolate Cake", 6.50, "https://images.unsplash.com/photo-1563729784474-d77dbb933a9e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80"),
                    Product(8, "Cheesecake", 7.00, "https://images.unsplash.com/photo-1542826438-62a34865269f?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80"),
                    Product(9, "Ice Cream Scoop", 3.00, "https://images.unsplash.com/photo-1580915411954-155191d58226?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80")
                )
            ),
            Category(
                name = "Drinks",
                products = listOf(
                    Product(10, "Coca-Cola", 2.50, "https://images.unsplash.com/photo-1554866585-CD94860890b7?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=764&q=80"),
                    Product(11, "Orange Juice", 3.00, "https://images.unsplash.com/photo-1600271886742-f049cd451bba?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=687&q=80"),
                    Product(12, "Water Bottle", 1.50, "https://images.unsplash.com/photo-1523961131990-5ea7c61b2107?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=774&q=80")
                )
            )
        )
    }
}
