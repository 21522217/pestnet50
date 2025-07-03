package com.vn.uit.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

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

data class ForgotPasswordRequest(
    val token: String,
    @SerializedName("email")
    val email: String
)

data class ForgotPasswordResponse(
    @SerializedName("status")
    val status: Int,
    @SerializedName("message")
    val message: String?,
    @SerializedName("success")
    val success: Boolean? = null
)

data class ReportApplicationRequest(
    val userId: UUID,
    val message: String
)
