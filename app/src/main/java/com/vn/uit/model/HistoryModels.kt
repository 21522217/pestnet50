package com.vn.uit.model

import java.time.Instant
import java.util.UUID

data class ClassificationResponse(
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
)