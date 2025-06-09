package com.vn.uit.model

data class AuthRequest(val email: String, val password: String)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val refreshToken: String,
    val username: String,
    val email: String,
    val userId: String
)

data class SignupRequest(
    val email: String,
    val username: String,
    val password: String
)

data class SignupResponse(
    val status: Int,
    val message: String,
)