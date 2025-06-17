package com.vn.uit.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class Pest(
    val id: String?,
    val name: String?,
    val regions: List<String>?,
    val scientificName: String?,
    val description: String?,
    val biologicalCharacteristics: String?,
    val controlMethods: String?,
    @SerializedName("harmLevel")
    private val _harmLevel: String?,
    val pestUrl: String = "",
    val relatedImages: List<String> = emptyList(),
    val pestInsecticide: List<String> = emptyList(),
    val occurrenceCount: Int = 0,
    val deleted: Boolean = false
) : Parcelable {

    val harmLevel: HarmLevel?
        get() = _harmLevel?.let { stringToHarmLevel(it) }

    private fun stringToHarmLevel(harmLevelString: String): HarmLevel? {
        return when (harmLevelString.lowercase(Locale.getDefault())) {
            "very low" -> HarmLevel.VERY_LOW
            "low" -> HarmLevel.LOW
            "medium", "moderate" -> HarmLevel.MODERATE
            "high" -> HarmLevel.HIGH
            "very high" -> HarmLevel.VERY_HIGH
            else -> null
        }
    }
}
@Parcelize
enum class HarmLevel(val displayName: String, val stars: Int) : Parcelable {
    VERY_LOW("Very Low", 1),
    LOW("Low", 2),
    MODERATE("Moderate", 3),
    HIGH("High", 4),
    VERY_HIGH("Very High", 5)
}

data class PestScreenState(
    val pests: List<Pest> = emptyList(),
    val filteredPests: List<Pest> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val selectedPest: Pest? = null,
    val showDialog: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLastPage: Boolean = false
)