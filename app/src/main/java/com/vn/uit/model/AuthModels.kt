// Improved Auth Models
package com.vn.uit.model

data class AuthRequest(val username: String, val password: String)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?  // Made nullable for better error handling
)

data class AuthData(
    val token: String,
    val userId: Long
)