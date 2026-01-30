package com.example.livraison.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.livraison.R
import com.example.livraison.model.Address
import com.example.livraison.model.Product
import com.example.livraison.viewmodel.AuthViewModel
import com.example.livraison.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    onCheckout: () -> Unit
) {
    val cart by mainViewModel.cart.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val addresses = authState.addresses
    val currentUserId = authState.user?.uid

    var selectedAddress by remember { mutableStateOf<Address?>(addresses.firstOrNull()) }
    var expanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(addresses) {
        if (selectedAddress == null) {
            selectedAddress = addresses.firstOrNull()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.my_cart)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Address Selector
                        Text(stringResource(id = R.string.deliver_to), style = MaterialTheme.typography.titleMedium)
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = selectedAddress?.name ?: stringResource(id = R.string.select_address),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                addresses.forEach { address ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(id = R.string.address_format, address.name, address.street)) },
                                        onClick = {
                                            selectedAddress = address
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(stringResource(id = R.string.total, cart.sumOf { it.price }), style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val uid = currentUserId ?: ""
                                if (uid.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.login_to_order)) }
                                    return@Button
                                }
                                if (selectedAddress == null) {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.select_delivery_address)) }
                                    return@Button
                                }

                                mainViewModel.createOrder(
                                    userId = uid,
                                    total = cart.sumOf { it.price },
                                    address = selectedAddress!!,
                                    onSuccess = { onCheckout() },
                                    onError = { errorMsg ->
                                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.order_error, errorMsg)) }
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedAddress != null
                        ) {
                            Text(stringResource(id = R.string.checkout))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (cart.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.cart_empty), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cart) { product ->
                        ProductCartItem(product)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCartItem(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(id = R.string.order_total_formatted, product.price), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
