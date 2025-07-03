package com.vn.uit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
data class ClassificationRequest(
    val imageUrl: String,
    val originalName: String,
    val scientificName: String,
    val modelName: String = "SWIN Transformer",
    val confidence: Float
)

@Parcelize
data class ClassificationResponse(
    val pestId: String,
    val pestName: String = "",
    val modelName: String = "",
    val confidence: Float = 0f,
    val classifiedAt: String?,
    val imageUrl: String = "",
    val pestRegions: List<String> = emptyList(),
    val pestScientificName: String = "",
    val pestDescription: String = "",
    val pestUrl: String = "",
    val pestInsecticide: List<String> = emptyList()
) : Parcelable

