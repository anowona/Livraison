package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.loginSuccess) {
        LaunchedEffect(Unit) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val role = doc.getString("role")
                    if (role.isNullOrEmpty()) {
                        navController.navigate("role_selection") { popUpTo("login") { inclusive = true } }
                    } else {
                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = uiState.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("Email") },
            isError = uiState.errorMessage != null
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = uiState.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.errorMessage != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { authViewModel.login() }, enabled = !uiState.loginInProgress) {
            if (uiState.loginInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("register") }) { Text("Register") }

        uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
