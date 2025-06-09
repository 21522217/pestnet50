package com.vn.uit.data.remote

import com.vn.uit.model.*
import retrofit2.http.*

interface HistoryApi {

    @POST("history/recent")
    suspend fun getRecentClassifications(
        @Body request: RecentScansRequest
    ): HistoryApiResponse<List<ClassificationResponse>>

    @POST("history/best-accuracy")
    suspend fun getBestAccuracyClassifications(
        @Body request: BestAccuracyRequest
    ): HistoryApiResponse<List<ClassificationResponse>>

    @POST("history/search")
    suspend fun searchClassifications(
        @Body request: SearchRequest
    ): HistoryApiResponse<List<ClassificationResponse>>

    @GET("history/stats")
    suspend fun getHistoryStats(): HistoryApiResponse<HistoryStats>

    @POST("history/refresh")
    suspend fun refreshCache(): HistoryApiResponse<Unit>

    @GET("history/classification/{id}")
    suspend fun getClassificationById(
        @Path("id") classificationId: String
    ): HistoryApiResponse<ClassificationResponse>
}
