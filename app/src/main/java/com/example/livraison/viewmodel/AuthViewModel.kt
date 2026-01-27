package com.example.livraison.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorMessage: String? = null,
    val profileUpdateMessage: String? = null,
    val loginInProgress: Boolean = false,
    val user: FirebaseUser? = null,
    val role: String? = null,
    val isRoleLoading: Boolean = true
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState(user = auth.currentUser))
    val uiState = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _uiState.update { it.copy(user = user) }
        if (user != null) {
            fetchUserRole(user)
        } else {
            _uiState.update { it.copy(role = null, isRoleLoading = false) }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun fetchUserRole(user: FirebaseUser) {
        _uiState.update { it.copy(isRoleLoading = true) }
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                _uiState.update { it.copy(role = role, isRoleLoading = false) }
                Log.d("AuthViewModel", "User role fetched: $role")
            }
            .addOnFailureListener { e ->
                Log.w("AuthViewModel", "Error fetching user role", e)
                _uiState.update { it.copy(role = null, isRoleLoading = false) } // Role is unknown
            }
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
            try {
                auth.signInWithEmailAndPassword(_uiState.value.email, _uiState.value.password).await()
                _uiState.update { it.copy(loginInProgress = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loginInProgress = false,
                        errorMessage = e.message ?: "Login failed. Check your credentials."
                    )
                }
                Log.e("AuthViewModel", "Login failed", e)
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

    fun updateProfile(displayName: String) {
        val user = auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(profileUpdateMessage = "No user is signed in.") }
            return
        }

        Log.d("AuthViewModel", "Updating profile for user ${user.uid} with displayName: '$displayName'")

        val profileUpdates = userProfileChangeRequest {
            this.displayName = displayName
        }

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("AuthViewModel", "updateProfile task was successful.")
                user.reload().addOnCompleteListener { reloadTask ->
                    if (reloadTask.isSuccessful) {
                        Log.d("AuthViewModel", "User reload was successful.")
                        _uiState.update {
                            it.copy(
                                user = auth.currentUser, // Use the newly reloaded user
                                profileUpdateMessage = "Profile updated successfully!"
                            )
                        }
                    } else {
                        Log.w("AuthViewModel", "User reload failed.", reloadTask.exception)
                        _uiState.update { it.copy(profileUpdateMessage = "Update succeeded, but failed to refresh data.") }
                    }
                }
            } else {
                Log.e("AuthViewModel", "updateProfile task failed.", task.exception)
                _uiState.update { it.copy(profileUpdateMessage = task.exception?.message ?: "An error occurred.") }
            }
        }
    }


    fun dismissProfileMessage() {
        _uiState.update { it.copy(profileUpdateMessage = null) }
    }

    fun logout() {
        auth.signOut()
    }
}
