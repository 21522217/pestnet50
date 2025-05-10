// Fixed MockAuthApi Implementation
package com.vn.uit.data.remote.mock

import com.vn.uit.data.remote.AuthApi
import com.vn.uit.model.AuthData
import com.vn.uit.model.AuthRequest
import com.vn.uit.model.AuthResponse

/**
 * A fake implementation of AuthApi that immediately returns
 * hard-coded JSON objects instead of doing real HTTP calls.
 */
class MockAuthApi : AuthApi {
    override suspend fun login(request: AuthRequest): AuthResponse {
        // Simulate successful login 
        return if (request.username.isNotEmpty() && request.password.isNotEmpty()) {
            AuthResponse(
                success = true,
                message = "Mock login successful for ${request.username}",
                data = AuthData(
                    token = "mock_jwt_token_${request.username}",
                    userId = 123
                )
            )
        } else {
            // Simulate login failure
            AuthResponse(
                success = false,
                message = "Invalid credentials",
                data = null
            )
        }
    }

    override suspend fun signup(request: AuthRequest): AuthResponse {
        // Implementation for signup
        return AuthResponse(
            success = true,
            message = "Mock signup successful for ${request.username}",
            data = AuthData(
                token = "mock_jwt_token_${request.username}",
                userId = 456
            )
        )
    }
}