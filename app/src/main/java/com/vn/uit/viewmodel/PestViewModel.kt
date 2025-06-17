package com.vn.uit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vn.uit.model.Pest
import com.vn.uit.model.PestScreenState
import com.vn.uit.repository.PestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PestViewModel(
    private val pestRepository: PestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PestScreenState())
    val uiState: StateFlow<PestScreenState> = _uiState.asStateFlow()

    init {
        loadPests()
    }

    fun loadPests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = pestRepository.getAllPests()
                if (result.isSuccess) {
                    val pestPage = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        pests = pestPage.pests,
                        filteredPests = pestPage.pests,
                        currentPage = 0,
                        totalPages = pestPage.pagination.totalPages,
                        isLastPage = pestPage.pagination.last,
                        isLoading = false,
                        isRefreshing = false
                    )

                    // Apply current search filter if any
                    val currentQuery = _uiState.value.searchQuery
                    if (currentQuery.isNotBlank()) {
                        searchPests(currentQuery)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false
                )
                // Log error for debugging
                e.printStackTrace()
            }
        }
    }

    fun refreshPests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                val result = pestRepository.getAllPests()
                if (result.isSuccess) {
                    val pestPage = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        pests = pestPage.pests,
                        filteredPests = pestPage.pests,
                        currentPage = 0,
                        totalPages = pestPage.pagination.totalPages,
                        isLastPage = pestPage.pagination.last,
                        isRefreshing = false
                    )

                    // Apply current search filter if any
                    val currentQuery = _uiState.value.searchQuery
                    if (currentQuery.isNotBlank()) {
                        searchPests(currentQuery)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
                e.printStackTrace()
            }
        }
    }

    fun loadMorePests() {
        if (_uiState.value.isLoading || _uiState.value.isLastPage) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val nextPage = _uiState.value.currentPage + 1
                val result = pestRepository.getAllPests(page = nextPage)
                if (result.isSuccess) {
                    val pestPage = result.getOrThrow()
                    val updatedList = _uiState.value.pests + pestPage.pests
                    _uiState.value = _uiState.value.copy(
                        pests = updatedList,
                        filteredPests = if (_uiState.value.searchQuery.isBlank()) {
                            updatedList
                        } else {
                            // Re-apply search filter to updated list
                            filterPests(updatedList, _uiState.value.searchQuery)
                        },
                        currentPage = nextPage,
                        totalPages = pestPage.pagination.totalPages,
                        isLastPage = pestPage.pagination.last,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                e.printStackTrace()
            }
        }
    }

    fun searchPests(query: String) {
        val filteredPests = if (query.isBlank()) {
            _uiState.value.pests
        } else {
            filterPests(_uiState.value.pests, query)
        }

        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredPests = filteredPests
        )
    }

    private fun filterPests(pests: List<Pest>, query: String): List<Pest> {
        return pests.filter { pest ->
            val nameMatches = pest.name?.contains(query, ignoreCase = true) == true
            val scientificNameMatches = pest.scientificName?.contains(query, ignoreCase = true) == true
            val regionMatches = pest.regions?.any { it.contains(query, ignoreCase = true) } == true
            nameMatches || scientificNameMatches || regionMatches
        }
    }

    fun showPestDetails(pest: Pest) {
        _uiState.value = _uiState.value.copy(
            selectedPest = pest,
            showDialog = true
        )
    }

    fun hidePestDetails() {
        _uiState.value = _uiState.value.copy(
            selectedPest = null,
            showDialog = false
        )
    }

    fun getPestById(pestId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = pestRepository.getPestById(pestId)
                if (result.isSuccess) {
                    val pest = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        selectedPest = pest,
                        showDialog = true,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                e.printStackTrace()
            }
        }
    }
}