package com.example.livraison.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.livraison.R
import com.example.livraison.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // The MainActivity router will handle navigation once the user is logged in.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(id = R.string.welcome_back), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(id = R.string.login_to_continue), style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text(stringResource(id = R.string.email)) },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = stringResource(id = R.string.email_icon)) },
            isError = uiState.errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text(stringResource(id = R.string.password)) },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = stringResource(id = R.string.password_icon)) },
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.login() },
            enabled = !uiState.loginInProgress,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (uiState.loginInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(id = R.string.login), style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(onClick = { navController.navigate("register") }) {
            Text(stringResource(id = R.string.no_account_register))
        }
    }
}
