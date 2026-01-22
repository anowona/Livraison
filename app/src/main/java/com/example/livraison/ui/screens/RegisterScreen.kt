package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.register(email, password) { success ->
                if (success) navController.navigate("role_selection") { popUpTo("register") { inclusive = true } }
                else errorMessage = "Registration failed. Check your email and password."
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Register") }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("login") }) { Text("Already have an account? Login") }

        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
