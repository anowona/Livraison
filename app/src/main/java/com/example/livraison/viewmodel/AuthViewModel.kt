package com.example.livraison.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val loginInProgress: Boolean = false,
    val loginSuccess: Boolean = false,
    val user: FirebaseUser? = null
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState(user = auth.currentUser))
    val uiState = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _uiState.update { it.copy(user = firebaseAuth.currentUser) }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun login() {
        if (!Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format.") }
            return
        }

        _uiState.update { it.copy(loginInProgress = true, errorMessage = null) }

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _uiState.update { it.copy(loginInProgress = false, loginSuccess = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                loginInProgress = false,
                                errorMessage = task.exception?.message ?: "Login failed. Check your credentials."
                            )
                        }
                        Log.e("AuthViewModel", "Login failed", task.exception)
                    }
                }
        }
    }

    fun register(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    Log.e("AuthViewModel", "Registration failed", task.exception)
                    callback(false)
                }
            }
    }


    fun logout() {
        auth.signOut()
        _uiState.update { it.copy(loginSuccess = false) }
    }
}
