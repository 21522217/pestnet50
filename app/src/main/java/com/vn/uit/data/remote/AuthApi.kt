package com.vn.uit.data.remote

import com.vn.uit.model.AuthRequest
import com.vn.uit.model.AuthResponse
import com.vn.uit.model.SignupRequest
import com.vn.uit.model.SignupResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): ApiResponse<AuthData>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse
}
