package com.example.livraison.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livraison.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val profileUpdateMessage: String? = null,
    val addressErrorMessage: String? = null, // For address errors
    val loginInProgress: Boolean = false,
    val registrationInProgress: Boolean = false,
    val registrationSuccess: Boolean = false,
    val user: FirebaseUser? = null,
    val role: String? = null,
    val isRoleLoading: Boolean = true,
    val addresses: List<Address> = emptyList()
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState(user = auth.currentUser))
    val uiState = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user != null) {
            _uiState.update { it.copy(user = user, isRoleLoading = true) }
            fetchUserRole(user)
            fetchUserAddresses(user.uid)
        } else {
            _uiState.update { it.copy(user = null, role = null, isRoleLoading = false, addresses = emptyList()) }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        _uiState.value.user?.uid?.let { fetchUserAddresses(it) }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    private fun fetchUserRole(user: FirebaseUser) {
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                val role = document.getString("role")
                _uiState.update { it.copy(role = role, isRoleLoading = false) }
            }
            .addOnFailureListener { e ->
                Log.w("AuthViewModel", "Error fetching user role", e)
                _uiState.update { it.copy(role = null, isRoleLoading = false) }
            }
    }

    private fun fetchUserAddresses(userId: String) {
        db.collection("users").document(userId).collection("addresses").get()
            .addOnSuccessListener { result ->
                val addresses = result.toObjects(Address::class.java)
                _uiState.update { it.copy(addresses = addresses) }
            }
            .addOnFailureListener { e ->
                Log.w("AuthViewModel", "Error fetching addresses", e)
            }
    }

    fun addAddress(context: Context, userId: String, address: Address) {
        viewModelScope.launch {
            _uiState.update { it.copy(addressErrorMessage = null) } // Clear previous errors
            try {
                val geocoder = Geocoder(context)
                val fullAddress = "${address.street}, ${address.city}, ${address.postalCode}"
                
                @Suppress("DEPRECATION")
                val geocoderResults = geocoder.getFromLocationName(fullAddress, 1)
                
                val finalAddress = if (geocoderResults != null && geocoderResults.isNotEmpty()) {
                    val location = geocoderResults[0]
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    address.copy(geoPoint = geoPoint)
                } else {
                    _uiState.update { it.copy(addressErrorMessage = "Address not found. Please check spelling.") }
                    null
                }

                if (finalAddress != null) {
                    db.collection("users").document(userId).collection("addresses").document(finalAddress.id).set(finalAddress).await()
                    fetchUserAddresses(userId) // Refresh list
                }

            } catch (e: IOException) {
                Log.e("AuthViewModel", "Geocoding failed for address: ${address.street}", e)
                _uiState.update { it.copy(addressErrorMessage = "Network error during address lookup.") }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error adding address to Firestore", e)
                _uiState.update { it.copy(addressErrorMessage = "Could not save address.") }
            }
        }
    }

    fun dismissAddressError() {
        _uiState.update { it.copy(addressErrorMessage = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(password: String) {
        _uiState.update { it.copy(confirmPassword = password, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format.") }
            return
        }
        if (state.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Password cannot be empty.") }
            return
        }
        
        _uiState.update { it.copy(loginInProgress = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(state.email, state.password).await()
                // Auth state listener will handle success
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

    fun register() {
        val state = _uiState.value
        if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format.") }
            return
        }
        if (state.password.length < 6) {
             _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters long.") }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        _uiState.update { it.copy(registrationInProgress = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(state.email, state.password).await()
                val newUser = authResult.user
                if (newUser != null) {
                    val userProfile = mapOf("role" to "client")
                    db.collection("users").document(newUser.uid).set(userProfile).await()
                    // Auth state listener will handle success
                    _uiState.update { it.copy(registrationInProgress = false, registrationSuccess = true) }
                } else {
                    throw IllegalStateException("User was null after registration.")
                }
            } catch (e: Exception) {
                 _uiState.update {
                    it.copy(
                        registrationInProgress = false,
                        errorMessage = e.message ?: "Registration failed."
                    )
                }
                Log.e("AuthViewModel", "Registration failed", e)
            }
        }
    }
    
    fun resetRegistrationStatus() {
        _uiState.update { it.copy(registrationSuccess = false) }
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

        viewModelScope.launch {
            try {
                user.updateProfile(profileUpdates).await()
                _uiState.update {
                    it.copy(
                        user = auth.currentUser, // Refresh user state
                        profileUpdateMessage = "Profile updated successfully!"
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "updateProfile task failed.", e)
                _uiState.update { it.copy(profileUpdateMessage = e.message ?: "An error occurred.") }
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