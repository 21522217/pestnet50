package com.vn.uit.repository

import com.vn.uit.data.remote.HistoryApi
import com.vn.uit.model.*

class HistoryRepository(
    private val historyApi: HistoryApi
) {

    suspend fun getRecentScans(limit: Int = 10): Result<List<Classification>> {
        return try {
            val response = historyApi.getRecentClassifications(RecentScansRequest(limit))
            if (response.success && response.data != null) {
                val classifications = response.data.map { it.toClassification() }
                Result.success(classifications)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch recent scans"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBestAccuracyResults(limit: Int = 10, minConfidence: Float = 0.7f): Result<List<Classification>> {
        return try {
            val response = historyApi.getBestAccuracyClassifications(
                BestAccuracyRequest(limit, minConfidence)
            )
            if (response.success && response.data != null) {
                val classifications = response.data
                    .map { it.toClassification() }
                    .sortedByDescending { it.confidence }
                Result.success(classifications)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch best accuracy results"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchClassifications(query: String): Result<List<Classification>> {
        return try {
            val response = historyApi.searchClassifications(SearchRequest(query))
            if (response.success && response.data != null) {
                val classifications = response.data.map { it.toClassification() }
                Result.success(classifications)
            } else {
                Result.failure(Exception(response.message ?: "Search failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistoryStats(): Result<HistoryStats> {
        return try {
            val response = historyApi.getHistoryStats()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to fetch stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAllData(): Result<Unit> {
        return try {
            // Trigger cache refresh on server side
            val response = historyApi.refreshCache()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Refresh failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension function to convert API response to domain model
private fun ClassificationResponse.toClassification(): Classification {
    return Classification(
        classificationId = this.classificationId,
        imageUrl = this.imageUrl,
        confidence = this.confidence,
        modelName = this.modelName,
        classifiedAt = this.classifiedAt,
        pestId = this.pestId,
        pestName = this.pestName,
        pestRegions = this.pestRegions,
        pestScientificName = this.pestScientificName,
        pestDescription = this.pestDescription,
        pestImageUrl = this.pestImageUrl,
        pestUrl = this.pestUrl,
        pestInsecticide = this.pestInsecticide
    )
}