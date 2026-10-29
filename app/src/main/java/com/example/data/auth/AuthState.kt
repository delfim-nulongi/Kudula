package com.example.data.auth

sealed class AuthState {
    data class Anonymous(
        val sessionId: String,
        val deviceId: String
    ) : AuthState()

    data class Authenticated(
        val uid: String,
        val email: String?,
        val displayName: String?,
        val isAnonymous: Boolean = false,
        val photoUrl: String? = null
    ) : AuthState()

    data object Loading : AuthState()

    data class Error(val message: String) : AuthState()
}
