package com.example.livraison.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livraison.data.repository.OrderRepository
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

    // -------------------------
    // Search
    // -------------------------
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // -------------------------
    // Load products
    // -------------------------
    private fun loadProducts() {
        viewModelScope.launch {
            _categories.value = getCategorizedMenu()
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
                    Product(1, "Classic Burger", 8.99, "https://cdn.pixabay.com/photo/2016/03/05/19/02/hamburger-1238246_1280.jpg"),
                    Product(2, "Cheeseburger", 9.99, "https://cdn.pixabay.com/photo/2017/08/06/00/28/burger-2589259_1280.jpg"),
                    Product(3, "Bacon Burger", 10.99, "https://cdn.pixabay.com/photo/2019/01/29/18/05/burger-3962496_1280.jpg")
                )
            ),
            Category(
                name = "Pizzas",
                products = listOf(
                    Product(4, "Margherita Pizza", 12.50, "https://cdn.pixabay.com/photo/2017/12/09/08/18/pizza-3007395_1280.jpg"),
                    Product(5, "Pepperoni Pizza", 14.00, "https://cdn.pixabay.com/photo/2020/05/17/04/22/pizza-5179939_1280.jpg"),
                    Product(6, "Vegetarian Pizza", 13.00, "https://cdn.pixabay.com/photo/2017/01/03/11/33/pizza-1949183_1280.jpg")
                )
            ),
            Category(
                name = "Desserts",
                products = listOf(
                    Product(7, "Chocolate Cake", 6.50, "https://cdn.pixabay.com/photo/2016/11/22/18/52/cake-1850011_1280.jpg"),
                    Product(8, "Cheesecake", 7.00, "https://cdn.pixabay.com/photo/2018/05/01/18/21/eclair-3366430_1280.jpg"),
                    Product(9, "Ice Cream Scoop", 3.00, "https://cdn.pixabay.com/photo/2017/06/29/20/09/ice-cream-2455593_1280.jpg")
                )
            ),
            Category(
                name = "Drinks",
                products = listOf(
                    Product(10, "Coca-Cola", 2.50, "https://cdn.pixabay.com/photo/2014/09/26/19/51/coca-cola-462776_1280.jpg"),
                    Product(11, "Orange Juice", 3.00, "https://cdn.pixabay.com/photo/2017/01/20/15/06/oranges-1995056_1280.jpg"),
                    Product(12, "Water Bottle", 1.50, "https://cdn.pixabay.com/photo/2018/01/07/16/07/water-3067838_1280.jpg")
                )
            )
        )
    }
}
