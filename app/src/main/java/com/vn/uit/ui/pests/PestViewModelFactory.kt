package com.vn.uit.ui.pests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vn.uit.repository.PestRepository
import com.vn.uit.viewmodel.PestViewModel

class PestViewModelFactory(
    private val pestRepository: PestRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PestViewModel::class.java)) {
            return PestViewModel(pestRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}