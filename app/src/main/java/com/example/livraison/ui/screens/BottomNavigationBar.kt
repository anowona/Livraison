package com.example.livraison.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.livraison.viewmodel.DriverViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun BottomNavigationBar(
    navController: NavController,
    userRole: String?,
    driverViewModel: DriverViewModel?
) {
    val items = when (userRole) {
        "livreur" -> listOf(
            NavigationItem.DriverDashboard,
            NavigationItem.Profile
        )

        else -> listOf(
            NavigationItem.Home,
            NavigationItem.Cart,
            NavigationItem.Profile
        )
    }

    val availableOrdersStateFlow = driverViewModel?.availableOrders ?: MutableStateFlow(emptyList())
    val availableOrders by availableOrdersStateFlow.collectAsState()
    val availableOrdersCount = availableOrders.size

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.route == NavigationItem.DriverDashboard.route) {
                        BadgedBox(
                            badge = {
                                // Only show the badge if there are orders
                                if (availableOrdersCount > 0) {
                                    Badge { Text(text = "$availableOrdersCount") }
                                }
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.title)
                        }
                    } else {
                        // Regular icon for other items
                        Icon(item.icon, contentDescription = item.title)
                    }
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Home")
    object Cart : NavigationItem("cart", Icons.Default.ShoppingCart, "Cart")
    object Profile : NavigationItem("profile", Icons.Default.Person, "Profile")
    object DriverDashboard : NavigationItem("driver_dashboard", Icons.Filled.Speed, "Dashboard")
}
