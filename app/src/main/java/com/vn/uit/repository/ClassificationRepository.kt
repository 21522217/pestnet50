package com.vn.uit.repository

import com.vn.uit.data.remote.ApiClient
import com.vn.uit.data.remote.ApiResponse
import com.vn.uit.model.ClassificationResponse

class ClassificationRepository {

    private val api = ApiClient.classificationApi

    suspend fun getRecentClassifications(): List<ClassificationResponse> {
        val response: ApiResponse<List<ClassificationResponse>> = api.getRecentClassifications()
        if (response.status == 200 && response.data != null) {
            return response.data
        } else {
            throw Exception(response.message ?: "Unknown error")
        }
    }
}