package com.vn.uit.ui.home

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.vn.uit.R
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.FragmentResultBinding
import com.vn.uit.model.ClassificationResponse
import com.vn.uit.ui.pests.PestDetailDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val args: ResultFragmentArgs by navArgs()
    private lateinit var classificationResult: ClassificationResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.insecticidesCard.visibility = View.GONE
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


        binding.pestNameTextView.setOnClickListener {
            val scientificName = classificationResult.pestScientificName
            if (!scientificName.isNullOrBlank()) {
                fetchPestAndShowDialog(scientificName)
            } else {
                showSnackbar(getString(R.string.no_scientific_name_available))
            }
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

    /**
     * Fetches detailed pest information and shows it in a dialog
     * @param scientificName The scientific name of the pest to fetch
     */
    private fun fetchPestAndShowDialog(scientificName: String) {

        if (scientificName.isBlank()) {
            showSnackbar("Scientific name is required to fetch pest details")
            return
        }

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Loading pest information...")
            setCancelable(false)
        }

        lifecycleScope.launch {
            try {

                progressDialog.show()


                val response = withContext(Dispatchers.IO) {
                    ApiClient.pestApi.getPestByScientificName(scientificName)
                }


                progressDialog.dismiss()


                when {
                    response.status == 200 && response.data != null -> {

                        val pest = response.data
                        val dialog = PestDetailDialog.newInstance(pest)
                        dialog.show(childFragmentManager, "pest_detail_dialog")
                    }
                    response.status == 404 -> {

                        showSnackbar("Pest information not found for: $scientificName")
                    }
                    else -> {

                        val errorMessage = response.message ?: "Failed to load pest information"
                        showSnackbar(errorMessage)
                    }
                }
            } catch (e: Exception) {

                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }


                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "No internet connection available"
                    is java.net.SocketTimeoutException -> "Request timed out. Please try again"
                    is retrofit2.HttpException -> {
                        when (e.code()) {
                            404 -> "Pest information not found"
                            500 -> "Server error. Please try again later"
                            else -> "Network error: ${e.message()}"
                        }
                    }
                    else -> "Error loading pest information: ${e.localizedMessage}"
                }

                showSnackbar(errorMessage)
            }
        }
    }

    private fun shareResult() {
        try {
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
    }

    private fun buildShareText(): String {
        val pestName = classificationResult.pestName ?: getString(R.string.unknown_pest)
        val scientificName = classificationResult.pestScientificName
        val confidence = (classificationResult.confidence * 100).toInt()
        val timestamp = classificationResult.classifiedAt?.let { formatTimestamp(it) }

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

            findNavController().popBackStack(R.id.nav_home, false)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}