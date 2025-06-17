package com.vn.uit.model

import java.time.Instant

data class HistoryApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val timestamp: Instant
)

data class RecentScansRequest(
    val limit: Int = 10,
    val offset: Int = 0
)

data class BestAccuracyRequest(
    val limit: Int = 10,
    val minConfidence: Float = 0.0f
)

data class SearchRequest(
    val query: String,
    val limit: Int = 20
)

data class HistoryStats(
    val totalScans: Int,
    val averageAccuracy: Float,
    val topPestName: String?,
    val lastScanDate: Instant?
)
