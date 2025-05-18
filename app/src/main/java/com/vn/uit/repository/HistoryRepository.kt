package com.vn.uit.data.repository

import com.vn.uit.data.remote.ApiService
import com.vn.uit.data.remote.ImageDetailResponse
import com.vn.uit.data.remote.ImageResponse
import com.vn.uit.data.remote.PestDetailResponse
import com.vn.uit.data.remote.PestResponse
import com.vn.uit.data.remote.SearchResponse
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Repository for handling history data
 */
class HistoryRepository(private val apiService: ApiService) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Get recent images from the API
     */
    suspend fun getRecentImages(): Result<List<ImageResponse>> {
        return try {
            val response = apiService.getRecentImages()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch recent images"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get best accuracy images from the API
     */
    suspend fun getBestAccuracyImages(): Result<List<ImageResponse>> {
        return try {
            val response = apiService.getBestAccuracyImages()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch best accuracy images"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get most detected pests from the API
     */
    suspend fun getMostDetectedPests(): Result<List<PestResponse>> {
        return try {
            val response = apiService.getMostDetectedPests()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch most detected pests"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get image details from the API
     */
    suspend fun getImageDetails(imageId: String): Result<ImageDetailResponse> {
        return try {
            val response = apiService.getImageDetails(imageId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("No data received for image details"))
                }
            } else {
                Result.failure(Exception("Failed to fetch image details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get pest details from the API
     */
    suspend fun getPestDetails(pestId: String): Result<PestDetailResponse> {
        return try {
            val response = apiService.getPestDetails(pestId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("No data received for pest details"))
                }
            } else {
                Result.failure(Exception("Failed to fetch pest details"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for images and pests
     */
    suspend fun search(query: String): Result<SearchResponse> {
        return try {
            val response = apiService.searchImages(query)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("No data received for search"))
                }
            } else {
                Result.failure(Exception("Failed to perform search"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}