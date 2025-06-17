package com.vn.uit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class ClassificationRequest(
    val imageUrl: String,
    val originalName: String,
    val scientificName: String,
    val modelName: String = "SWIN Transformer",
    val confidence: Float
)

@Parcelize
data class ClassificationResponse(
    val pestId: @RawValue UUID,
    val pestName: String?,
    val modelName: String,
    val confidence: Float,
    val classifiedAt: String,
    val imageUrl: String,
    val pestRegions: List<String>?,
    val pestScientificName: String,
    val pestDescription: String?,
    val pestUrl: String?,
    val pestInsecticide: List<String>?
) : Parcelable