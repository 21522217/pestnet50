package com.vn.uit.data.remote

import java.util.UUID

data class ApiResponse<T>(
    val status: Int,
    val message: String? = null,
    val error: String? = null,
    val data: T,
    val pagination: Pagination? = null
)

data class AuthData(
    val token: String,
    val refreshToken: String,
    val username: String,
    val email: String,
    val userId: String
)