package com.vn.uit.ui.history

import java.util.Date

data class HistoryItem(
    val id: String,
    val imageUrl: String,
    val uploadedAt: Date,
    val originalName: String,
    val confidence: Float = 0.0f
)

data class PestItem(
    val id: String,
    val name: String,
    val scientificName: String,
    val imageUrl: String,
    val occurrenceCount: Int = 0,
    val description: String
)

data class DetectedPestItem(
    val id: String,
    val pestId: String,
    val name: String,
    val scientificName: String,
    val imageUrl: String,
    val confidence: Float
)