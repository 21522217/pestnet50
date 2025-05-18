package com.vn.uit.ui.history
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vn.uit.data.remote.ImageDetailResponse
import com.vn.uit.data.remote.ImageResponse
import com.vn.uit.data.remote.PestDetailResponse
import com.vn.uit.data.remote.PestResponse
import com.vn.uit.data.repository.HistoryRepository
import com.vn.uit.ui.history.DetectedPestItem
import com.vn.uit.ui.history.HistoryItem
import com.vn.uit.ui.history.PestItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    private val _recentImages = MutableLiveData<List<HistoryItem>>()
    val recentImages: LiveData<List<HistoryItem>> = _recentImages
    private val _bestAccuracyImages = MutableLiveData<List<HistoryItem>>()
    val bestAccuracyImages: LiveData<List<HistoryItem>> = _bestAccuracyImages
    private val _mostDetectedPests = MutableLiveData<List<PestItem>>()
    val mostDetectedPests: LiveData<List<PestItem>> = _mostDetectedPests
    private val _selectedImageDetails = MutableLiveData<Pair<HistoryItem, List<DetectedPestItem>>>()
    val selectedImageDetails: LiveData<Pair<HistoryItem, List<DetectedPestItem>>> = _selectedImageDetails
    private val _selectedPestDetails = MutableLiveData<Pair<PestItem, List<HistoryItem>>>()
    val selectedPestDetails: LiveData<Pair<PestItem, List<HistoryItem>>> = _selectedPestDetails
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    init {
        loadHistoryData()
    }
    fun loadHistoryData() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                // Fetch recent images
                repository.getRecentImages().fold(
                    onSuccess = { images ->
                        _recentImages.value = convertToHistoryItems(images)
                    },
                    onFailure = { e ->
                        _error.value = "Failed to load recent images: ${e.message}"
                    }
                )
                // Fetch best accuracy images
                repository.getBestAccuracyImages().fold(
                    onSuccess = { images ->
                        _bestAccuracyImages.value = convertToHistoryItems(images)
                    },
                    onFailure = { e ->
                        _error.value = "Failed to load best accuracy images: ${e.message}"
                    }
                )
                // Fetch most detected pests
                repository.getMostDetectedPests().fold(
                    onSuccess = { pests ->
                        _mostDetectedPests.value = convertToPestItems(pests)
                    },
                    onFailure = { e ->
                        _error.value = "Failed to load most detected pests: ${e.message}"
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPestDetails(pestId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                repository.getPestDetails(pestId).fold(
                    onSuccess = { response ->
                        // Convert API response to UI models
                        val pestItem = convertToPestItem(response.pest)
                        val historyItems = convertToHistoryItems(response.detectedImages)
                        _selectedPestDetails.value = Pair(pestItem, historyItems)
                    },
                    onFailure = { e ->
                        _error.value = "Failed to load pest details: ${e.message}"
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    private fun convertToHistoryItems(images: List<ImageResponse>): List<HistoryItem> {
        return images.map { image ->
            convertToHistoryItem(image)
        }
    }

    private fun convertToHistoryItem(image: ImageResponse): HistoryItem {
        return HistoryItem(
            id = image.id,
            imageUrl = image.imageUrl,
            uploadedAt = parseDateTime(image.uploadedAt),
            originalName = image.originalName,
            confidence = image.confidence ?: 0.0f
        )
    }

    private fun convertToPestItems(pests: List<PestResponse>): List<PestItem> {
        return pests.map { pest ->
            convertToPestItem(pest)
        }
    }

    private fun convertToPestItem(pest: PestResponse): PestItem {
        return PestItem(
            id = pest.id,
            name = pest.name,
            scientificName = pest.scientificName,
            imageUrl = pest.imageUrl,
            occurrenceCount = pest.occurrenceCount ?: 0,
            description = pest.description ?: "No Description."
        )
    }

    private fun parseDateTime(dateString: String?): Date {
        return dateString?.let {
            try {
                dateFormat.parse(it) ?: Date()
            } catch (e: Exception) {
                Date()
            }
        } ?: Date()
    }
}