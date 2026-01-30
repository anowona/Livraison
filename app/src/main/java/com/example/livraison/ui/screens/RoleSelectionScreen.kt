package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.livraison.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun RoleSelectionScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val unknownError = stringResource(id = R.string.unknown_error)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(id = R.string.choose_your_role), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        listOf("client", "livreur").forEach { role ->
            Button(onClick = {
                isLoading = true
                val uid = auth.currentUser?.uid ?: return@Button
                db.collection("users").document(uid)
                    .set(mapOf("role" to role), SetOptions.merge())
                    .addOnSuccessListener {
                        // The router in MainActivity will handle navigation
                        isLoading = false
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.message ?: unknownError
                    }
            }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(role.replaceFirstChar { it.uppercase() })
            }
        }

        if (isLoading) CircularProgressIndicator()
        if (errorMessage.isNotEmpty()) Text(errorMessage, color = MaterialTheme.colorScheme.error)
    }
}
