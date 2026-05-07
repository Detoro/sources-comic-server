package com.toro.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
    val username: String? = null
)

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String
)

@Serializable
data class UserProfile(
    val id: String,
    val username: String
)