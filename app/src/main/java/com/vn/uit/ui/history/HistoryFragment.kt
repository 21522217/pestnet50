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
import com.vn.uit.repository.HistoryRepository
import com.vn.uit.ui.dialog.ClassificationDetailDialogFragment
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

class HistoryFragment : Fragment() {

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
                recentAdapter.submitList(createMockClassifications())
            } catch (e: Exception) {
                showToast("Error loading recent scans")
            }
        }

        lifecycleScope.launch {
            try {
                bestAccuracyAdapter.submitList(createMockClassifications())
            } catch (e: Exception) {
                showToast("Error loading best accuracy results")
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

    private fun createMockClassifications(): List<ClassificationResponse> {
        val now = System.currentTimeMillis()
        return listOf(
            ClassificationResponse(
                classificationId = UUID.randomUUID(),
                pestId = UUID.randomUUID(),
                pestName = "Aphid",
                modelName = "PestNet v1.0",
                confidence = 0.95f,
                classifiedAt = Instant.ofEpochMilli(now),
                imageUrl = "https://example.com/aphid.jpg",
                pestRegions = listOf("Asia", "Europe"),
                pestScientificName = "Aphis gossypii",
                pestDescription = "Aphids are small sap-sucking insects.",
                pestImageUrl = "https://example.com/aphid_pest.jpg",
                pestUrl = "https://en.wikipedia.org/wiki/Aphid",
                pestInsecticide = listOf("Insecticide A", "Insecticide B")
            ),
            ClassificationResponse(
                classificationId = UUID.randomUUID(),
                pestId = UUID.randomUUID(),
                pestName = "Spider Mite",
                modelName = "PestNet v1.0",
                confidence = 0.88f,
                classifiedAt = Instant.ofEpochMilli(now - 86_400_000L),
                imageUrl = "https://example.com/spidermite.jpg",
                pestRegions = listOf("North America"),
                pestScientificName = "Tetranychus urticae",
                pestDescription = "Spider mites are tiny arachnids that feed on plants with a lot of unhealthy things with a lot of unhealthy things with a lot of unhealthy things with a lot of unhealthy things with a lot of unhealthy things with a lot of unhealthy things with a lot of unhealthy things like larva katana Lorem ipsum myth asteroid naga sea of doom by my hand.",
                pestImageUrl = "https://example.com/spidermite_pest.jpg",
                pestUrl = "https://en.wikipedia.org/wiki/Spider_mite",
                pestInsecticide = listOf("Insecticide C")
            ),
            ClassificationResponse(
                classificationId = UUID.randomUUID(),
                pestId = UUID.randomUUID(),
                pestName = "Whitefly",
                modelName = "PestNet v1.0",
                confidence = 0.92f,
                classifiedAt = Instant.ofEpochMilli(now - 172_800_000L),
                imageUrl = "https://example.com/whitefly.jpg",
                pestRegions = listOf("Africa", "South America"),
                pestScientificName = "Bemisia tabaci",
                pestDescription = "Whiteflies are small hemipterans that feed on plant sap.",
                pestImageUrl = "https://example.com/whitefly_pest.jpg",
                pestUrl = "https://en.wikipedia.org/wiki/Whitefly",
                pestInsecticide = listOf("Insecticide D", "Insecticide E")
            )
        )
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
