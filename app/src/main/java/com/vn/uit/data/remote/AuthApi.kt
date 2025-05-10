package com.vn.uit.data.remote

import com.vn.uit.model.AuthRequest
import com.vn.uit.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): AuthResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: AuthRequest): AuthResponse
}
