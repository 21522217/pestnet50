package com.vn.uit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.vn.uit.databinding.FragmentHistoryBinding
import com.vn.uit.model.ClassificationResponse
import com.vn.uit.repository.ClassificationRepository
import com.vn.uit.ui.dialog.ClassificationDetailDialogFragment
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private val classificationRepository = ClassificationRepository()
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recentAdapter: ClassificationAdapter
    private lateinit var bestAccuracyAdapter: ClassificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupClickListeners()
        loadData()
    }

    private fun setupRecyclerViews() {
        recentAdapter = ClassificationAdapter(emptyList()) { classification ->
            openClassificationDetailDialog(classification)
        }
        binding.recentRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recentAdapter
        }

        bestAccuracyAdapter = ClassificationAdapter(emptyList()) { classification ->
            openClassificationDetailDialog(classification)
        }
        binding.bestAccuracyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = bestAccuracyAdapter
        }
    }

    private fun setupClickListeners() {
        binding.searchEditText.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchClassifications(query)
            }
            true
        }

        binding.viewAllRecent.setOnClickListener {
            showToast("View all recent scans clicked")
        }

        binding.viewAllAccuracy.setOnClickListener {
            showToast("View all best accuracy clicked")
        }

        binding.scanFab.setOnClickListener {
            showToast("Scan button clicked")
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val recent = classificationRepository.getRecentClassifications()
                recentAdapter.submitList(recent)
            } catch (e: Exception) {
                showToast("Error loading recent scans: ${e.message}")
            }
        }

        lifecycleScope.launch {
            try {
                val best = classificationRepository.getRecentClassifications()
                bestAccuracyAdapter.submitList(best)
            } catch (e: Exception) {
                showToast("Error loading best accuracy results: ${e.message}")
            }
        }
    }


    private fun searchClassifications(query: String) {
        lifecycleScope.launch {
            try {
                showToast("Searching for: $query")
            } catch (e: Exception) {
                showToast("Search error")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun openClassificationDetailDialog(classification: ClassificationResponse) {
        val relatedImages = listOf<String>() // Add URLs if you want to show related images
        val dialog = ClassificationDetailDialogFragment(
            classification = classification,
            relatedImages = relatedImages,
            onShare = {
                Toast.makeText(requireContext(), "Sharing classification...", Toast.LENGTH_SHORT).show()
            },
            onRemove = {
                Toast.makeText(requireContext(), "Classification removed", Toast.LENGTH_SHORT).show()
            }
        )
        dialog.show(parentFragmentManager, "ClassificationDetailDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
