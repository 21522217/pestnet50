package com.vn.uit.model

import java.time.Instant
import java.util.UUID

data class Classification(
    val classificationId: UUID,
    val imageUrl: String,
    val confidence: Float,
    val modelName: String,
    val classifiedAt: Instant,
    val pestId: UUID,
    val pestName: String,
    val pestRegions: List<String>?,
    val pestScientificName: String?,
    val pestDescription: String?,
    val pestImageUrl: String?,
    val pestUrl: String?,
    val pestInsecticide: List<String>?
) {
    // Convenience properties for UI
    val confidencePercentage: Int
        get() = (confidence * 100).toInt()

    val formattedConfidence: String
        get() = "${confidencePercentage}%"

    val displayImageUrl: String
        get() = pestImageUrl ?: imageUrl

    val hasInsecticide: Boolean
        get() = !pestInsecticide.isNullOrEmpty()

    val insecticideList: String
        get() = pestInsecticide?.joinToString(", ") ?: "No insecticide data"

    val regionsList: String
        get() = pestRegions?.joinToString(", ") ?: "Unknown regions"
}