package com.vn.uit.data.remote

import com.vn.uit.model.ClassificationRequest
import com.vn.uit.model.ClassificationResponse
import retrofit2.http.*

interface ClassificationApi {

    @POST("classifications")
    suspend fun createClassification(
        @Body request: ClassificationRequest
    ): ApiResponse<ClassificationResponse>

    @DELETE("classifications/{id}")
    suspend fun softDeleteClassification(
        @Path("id") id: String
    ): ApiResponse<Unit>

    @GET("classifications/recent")
    suspend fun getRecentClassifications(): ApiResponse<List<ClassificationResponse>>

    @GET("classifications/best")
    suspend fun getBestConfidenceClassifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "confidence",
        @Query("sortDir") sortDir: String = "desc"
    ): ApiResponse<List<ClassificationResponse>>

    @GET("classifications/{id}")
    suspend fun getClassificationById(
        @Path("id") id: String
    ): ApiResponse<ClassificationResponse>
}
