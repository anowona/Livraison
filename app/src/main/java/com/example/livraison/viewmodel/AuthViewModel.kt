package com.example.livraison.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""
    val currentUserEmail: String?
        get() = auth.currentUser?.email

    // ✅ Inscription
    fun register(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // ✅ Connexion
    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful) // retourne true si succès, false sinon
            }
    }

    // ✅ Déconnexion
    fun logout() {
        auth.signOut()
    }

}
