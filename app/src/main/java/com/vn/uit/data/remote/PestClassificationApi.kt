package com.vn.uit.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service interface for pest classification data
 */
interface ApiService {

    @GET("images/recent")
    suspend fun getRecentImages(): Response<List<ImageResponse>>

    @GET("images/best-accuracy")
    suspend fun getBestAccuracyImages(): Response<List<ImageResponse>>

    @GET("pests/most-detected")
    suspend fun getMostDetectedPests(): Response<List<PestResponse>>

    @GET("images/{imageId}")
    suspend fun getImageDetails(@Path("imageId") imageId: String): Response<ImageDetailResponse>

    @GET("pests/{pestId}")
    suspend fun getPestDetails(@Path("pestId") pestId: String): Response<PestDetailResponse>

    @GET("images/search")
    suspend fun searchImages(@Query("query") query: String): Response<SearchResponse>
}

/**
 * Data classes for API responses
 */
data class ImageResponse(
    val id: String,
    val imageUrl: String,
    val uploadedAt: String,
    val originalName: String,
    val userId: String,
    val confidence: Float? = null
)

data class PestResponse(
    val id: String,
    val name: String,
    val scientificName: String,
    val imageUrl: String,
    val occurrenceCount: Int,
    val description: String
)

data class ClassificationResponse(
    val id: String,
    val pestId: String,
    val pestName: String,
    val pestScientificName: String,
    val pestImageUrl: String,
    val confidence: Float,
    val modelId: String,
    val classifiedAt: String
)

data class ImageDetailResponse(
    val image: ImageResponse,
    val classifications: List<ClassificationResponse>
)

data class PestDetailResponse(
    val pest: PestResponse,
    val description: String,
    val detectedImages: List<ImageResponse>
)

data class SearchResponse(
    val images: List<ImageResponse>,
    val pests: List<PestResponse>
)