package com.vn.uit.ui.home // Changed package name to match your project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs // Add this import
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.vn.uit.R
import com.vn.uit.databinding.FragmentResultBinding
import com.vn.uit.model.ClassificationResponse
import java.text.SimpleDateFormat
import java.util.*

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    // Use Safe Args instead of manual Bundle handling
    private val args: ResultFragmentArgs by navArgs()
    private lateinit var classificationResult: ClassificationResponse

    // Remove the companion object - not needed with Safe Args

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the result from Safe Args
        classificationResult = args.result
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateViews()
        setupClickListeners()
    }

    private fun populateViews() {
        populateImageAndConfidence()
        populateIdentificationInfo()
        populateTreatmentInfo()
        binding.insecticidesCard.visibility = View.GONE // Remove card since no adapter is used
    }

    private fun populateImageAndConfidence() {
        Glide.with(this)
            .load(classificationResult.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.image_broken)
            .into(binding.resultImageView)

        val confidencePercentage = (classificationResult.confidence * 100).toInt()
        binding.confidenceTextView.text = getString(R.string.confidence_percentage, confidencePercentage)
    }

    private fun populateIdentificationInfo() {
        binding.pestNameTextView.text = classificationResult.pestName ?: getString(R.string.unknown_pest)
        binding.scientificNameTextView.text = classificationResult.pestScientificName
        setupRegionChips()
    }

    private fun setupRegionChips() {
        binding.regionsChipGroup.removeAllViews()

        classificationResult.pestRegions?.forEach { region ->
            val chip = Chip(requireContext()).apply {
                text = region
                isClickable = false
                isCheckable = false
                setChipBackgroundColorResource(R.color.chip_background)
                setTextColor(resources.getColor(R.color.chip_text, null))
            }
            binding.regionsChipGroup.addView(chip)
        }

        binding.regionsChipGroup.visibility = if (classificationResult.pestRegions.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun populateTreatmentInfo() {
        val treatmentText = classificationResult.pestDescription ?: getString(R.string.no_treatment_info)
        binding.treatmentTextView.text = treatmentText
    }

    private fun setupClickListeners() {
        binding.learnMoreButton.setOnClickListener {
            openExternalUrl(classificationResult.pestUrl)
        }

        binding.shareResultButton.setOnClickListener {
            shareResult()
        }

        binding.newScanButton.setOnClickListener {
            navigateToNewScan()
        }
    }

    private fun openExternalUrl(url: String?) {
        if (url.isNullOrBlank()) {
            showSnackbar(getString(R.string.no_url_available))
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                showSnackbar(getString(R.string.no_browser_available))
            }
        } catch (e: Exception) {
            showSnackbar(getString(R.string.error_opening_url))
        }
    }

    private fun shareResult(): Unit = try {
        val shareText = buildShareText()
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pest_identification_result))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_results)))
    } catch (e: Exception) {
        showSnackbar(getString(R.string.error_sharing))
    }

    private fun buildShareText(): String {
        val pestName = classificationResult.pestName ?: getString(R.string.unknown_pest)
        val scientificName = classificationResult.pestScientificName
        val confidence = (classificationResult.confidence * 100).toInt()
        val timestamp = formatTimestamp(classificationResult.classifiedAt)

        return buildString {
            appendLine(getString(R.string.share_header))
            appendLine()
            appendLine("🐛 ${getString(R.string.pest_name)}: $pestName")
            appendLine("🔬 ${getString(R.string.scientific_name)}: $scientificName")
            appendLine("📊 ${getString(R.string.confidence)}: $confidence%")
            appendLine("📅 ${getString(R.string.detected_on)}: $timestamp")

            classificationResult.pestRegions?.let { regions ->
                if (regions.isNotEmpty()) {
                    appendLine("🌍 ${getString(R.string.regions)}: ${regions.joinToString(", ")}")
                }
            }

            classificationResult.pestUrl?.let { url ->
                appendLine()
                appendLine("🔗 ${getString(R.string.learn_more)}: $url")
            }

            appendLine()
            appendLine(getString(R.string.app_signature))
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            timestamp
        }
    }

    private fun navigateToNewScan() {
        try {
            findNavController().navigate(
                ResultFragmentDirections.actionResultFragmentToNavHome()
            )
        } catch (e: Exception) {
            // Fallback to popBackStack if the action doesn't work
            findNavController().popBackStack(R.id.nav_home, false)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}