package com.vn.uit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.vn.uit.R
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.FragmentResultBinding
import com.vn.uit.model.InsecticideInfo
import com.vn.uit.viewmodel.PestClassificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import kotlin.math.roundToInt

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PestClassificationViewModel by activityViewModels()
    private lateinit var insecticidesAdapter: InsecticidesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        insecticidesAdapter = InsecticidesAdapter()
        binding.insecticidesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.insecticidesRecyclerView.adapter = insecticidesAdapter
    }

    private fun observeViewModel() {
        viewModel.detectedPestName.observe(viewLifecycleOwner) { pestName ->
            if (pestName != null) {
                displayPestInfo(pestName)
            } else {
                displayEmptyState()
            }
        }

        viewModel.processedImage.observe(viewLifecycleOwner) { bitmap ->
            binding.resultImageView.setImageBitmap(bitmap ?: return@observe)
        }

        viewModel.confidenceScore.observe(viewLifecycleOwner) { confidence ->
            val percentage = (confidence?.times(100))?.roundToInt() ?: 0
            binding.confidenceTextView.text = "$percentage% Confidence"
        }
    }

    private fun setupClickListeners() {
        binding.newScanButton.setOnClickListener {
            viewModel.resetClassification()
            findNavController().popBackStack()
            findNavController().popBackStack()
        }

        binding.learnMoreButton.setOnClickListener {
            Toast.makeText(requireContext(), "Learn more feature coming soon!", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun displayPestInfo(pestName: String) {
        lifecycleScope.launch {
            val encodedName = URLEncoder.encode(pestName, "UTF-8")
            val pest = withContext(Dispatchers.IO) {
                try {
                    ApiClient.pestApi.getPestByScientificName(encodedName).data
                } catch (e: Exception) {
                    null
                }
            }

            if (pest != null) {
                binding.pestNameTextView.text = pest.name
                binding.scientificNameTextView.text = pest.scientificName
                binding.treatmentTextView.text = pest.controlMethods
                pest.regions?.let { setupRegionChips(it) }

                val mappedInsecticides = pest.pestInsecticide?.mapNotNull {
                    val parts = it.split(" - ")
                    if (parts.size == 3) {
                        InsecticideInfo(parts[0].trim(), parts[1].trim(), parts[2].trim())
                    } else null
                }

                insecticidesAdapter.submitList(mappedInsecticides)
            } else {
                displayEmptyState()
            }
        }
    }


    private fun displayEmptyState() {
        binding.pestNameTextView.text = "Unknown Pest"
        binding.scientificNameTextView.text = "Classification unavailable"
        binding.treatmentTextView.text =
            "For accurate pest identification and treatment recommendations, please:\n• Take a clearer photo\n• Ensure good lighting\n• Focus on the pest details\n• Try from different angles"
        binding.regionsChipGroup.removeAllViews()
        insecticidesAdapter.submitList(emptyList())
        setupRegionChips(listOf("Various regions"))
    }

    private fun setupRegionChips(regions: List<String>) {
        binding.regionsChipGroup.removeAllViews()
        regions.forEach { region ->
            val chip = Chip(requireContext()).apply {
                text = region
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(com.google.android.material.R.color.m3_chip_background_color)
                setTextColor(
                    resources.getColor(
                        com.google.android.material.R.color.m3_chip_text_color,
                        null
                    )
                )
            }
            binding.regionsChipGroup.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
