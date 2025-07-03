package com.vn.uit.ui.pests

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.vn.uit.R
import com.vn.uit.databinding.PestDetailDialogBinding
import com.vn.uit.model.Pest
import com.vn.uit.model.HarmLevel

class PestDetailDialog : DialogFragment() {

    private var _binding: PestDetailDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_PEST = "arg_pest"

        fun newInstance(pest: Pest): PestDetailDialog {
            return PestDetailDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PEST, pest)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PestDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pest = requireArguments().getParcelable<Pest>(ARG_PEST)
        if (pest == null) {
            showError("Failed to load pest information")
            dismiss()
            return
        }

        setupViews(pest)
    }

    private fun setupViews(pest: Pest) {
        with(pest) {
            setupBasicInfo(this)
            setupHarmLevelBadge(this)
            setupOccurrenceCount(this)
            setupRegionsAffected(this)
            setupBiologicalCharacteristics(this)
            setupControlMethods(this)
            setupInsecticides(this)
            setupRelatedImages(this)
            setupStatistics(this)
            setupActionButtons(this)
        }
    }

    private fun setupBasicInfo(pest: Pest) {
        binding.apply {
            // Set pest name and scientific name
            pestName.text = pest.name ?: getString(R.string.unknown_pest)
            pestScientificName.text = pest.scientificName ?: getString(R.string.scientific_name_not_available)
            pestDescription.text = pest.description ?: getString(R.string.no_description_available)

            // Load main image
            val imageUrl = pest.relatedImages?.firstOrNull()
            pestImage.load(imageUrl) {
                placeholder(R.drawable.placeholder_image)
                error(R.drawable.image_placeholder)
                crossfade(true)
            }
        }
    }

    private fun setupHarmLevelBadge(pest: Pest) {
        binding.harmLevelBadge.apply {
            val harmLevel = pest.harmLevel

            text = harmLevel?.let { "${it.displayName.uppercase()} RISK" }
                ?: getString(R.string.unknown_risk)

            val badgeColor = getHarmLevelColor(harmLevel)
            setBackgroundColor(ContextCompat.getColor(requireContext(), badgeColor))
        }
    }

    private fun getHarmLevelColor(harmLevel: HarmLevel?): Int {
        return when (harmLevel) {
            HarmLevel.VERY_HIGH, HarmLevel.HIGH -> R.color.risk_high
            HarmLevel.MODERATE -> R.color.risk_medium
            HarmLevel.LOW, HarmLevel.VERY_LOW -> R.color.risk_low
            null -> R.color.risk_medium
        }
    }

    private fun setupOccurrenceCount(pest: Pest) {
        val occurrenceCount = (pest.occurrenceCount ?: 0).toString()
        binding.apply {
            // Set occurrence count in the top badge
            this.occurrenceCount.text = occurrenceCount
            // Set occurrence count in the statistics section
            occurrenceCountDetail.text = occurrenceCount
        }
    }

    private fun setupRegionsAffected(pest: Pest) {
        val regions = pest.regions ?: emptyList()
        val regionsText = if (regions.isNotEmpty()) {
            regions.joinToString(" • ")
        } else {
            getString(R.string.no_regions_specified)
        }

        // Set regions text in the dedicated regions affected section
        binding.regionsAffected.text = regionsText
    }

    private fun setupBiologicalCharacteristics(pest: Pest) {
        binding.biologicalCharacteristics.text = pest.biologicalCharacteristics
            ?: getString(R.string.no_biological_characteristics_available)
    }

    private fun setupControlMethods(pest: Pest) {
        binding.controlMethods.text = pest.controlMethods
            ?: getString(R.string.no_control_methods_available)
    }

    private fun setupInsecticides(pest: Pest) {
        binding.insecticidesContainer.apply {
            removeAllViews()

            val insecticides = pest.pestInsecticide ?: emptyList()

            if (insecticides.isEmpty()) {
                addView(createInsecticideCard(getString(R.string.no_insecticides_specified)))
            } else {
                insecticides.forEach { insecticide ->
                    addView(createInsecticideCard(insecticide))
                }
            }
        }
    }

    private fun createInsecticideCard(insecticideName: String): MaterialCardView {
        return MaterialCardView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_small)
                bottomMargin = resources.getDimensionPixelSize(R.dimen.spacing_xs)
            }

            // Set card styling to match layout
            cardElevation = 0f
            radius = resources.getDimensionPixelSize(R.dimen.corner_radius_medium).toFloat()
            strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_width_thin)
            strokeColor = ContextCompat.getColor(requireContext(), R.color.outline_dark)
            setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surface_light))

            addView(TextView(requireContext()).apply {
                text = insecticideName
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.padding_medium),
                    resources.getDimensionPixelSize(R.dimen.padding_small),
                    resources.getDimensionPixelSize(R.dimen.padding_medium),
                    resources.getDimensionPixelSize(R.dimen.padding_small)
                )
                textSize = 12f
                maxLines = 2
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
            })
        }
    }

    private fun setupRelatedImages(pest: Pest) {
        val relatedImages = pest.relatedImages
        if (!relatedImages.isNullOrEmpty()) {
            val adapter = RelatedImagesAdapter(relatedImages) { imageUrl ->
                openImageFullScreen(imageUrl)
            }

            binding.relatedImagesRecycler.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                this.adapter = adapter
                visibility = View.VISIBLE
            }
        } else {
            binding.relatedImagesRecycler.visibility = View.GONE
        }
    }

    private fun setupStatistics(pest: Pest) {
        binding.apply {
            // Set occurrence count detail
            occurrenceCountDetail.text = (pest.occurrenceCount ?: 0).toString()

            // Set harm level text and color
            harmLevelText.apply {
                val harmLevel = pest.harmLevel
                text = harmLevel?.displayName?.uppercase() ?: getString(R.string.unknown)

                val harmColor = getHarmLevelTextColor(harmLevel)
                setTextColor(ContextCompat.getColor(requireContext(), harmColor))
            }

            // Set regions count
            regionsCount.text = (pest.regions?.size ?: 0).toString()
        }
    }

    private fun getHarmLevelTextColor(harmLevel: HarmLevel?): Int {
        return when (harmLevel) {
            HarmLevel.VERY_HIGH, HarmLevel.HIGH -> R.color.error_red
            HarmLevel.MODERATE -> R.color.othersOrange
            HarmLevel.LOW, HarmLevel.VERY_LOW -> R.color.accent_green
            null -> R.color.text_secondary
        }
    }

    private fun setupActionButtons(pest: Pest) {
        binding.apply {
            // iNaturalist button
            btnViewOnINaturalist.setOnClickListener {
                val scientificName = pest.scientificName ?: pest.name ?: "unknown"
                openINaturalistSearch(scientificName)
            }

            // Share button
            btnShare.setOnClickListener {
                sharePestInfo(pest)
            }

            // Report sighting button
            btnReportSighting.setOnClickListener {
                reportPestSighting(pest)
            }

            // Get help button
            btnGetHelp.setOnClickListener {
                getHelpForPest(pest)
            }
        }
    }

    private fun openImageFullScreen(imageUrl: String) {
        try {
            // Option 1: Open in external browser/gallery
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                showError("Cannot open image")
            }
        } catch (e: Exception) {
            showError("Cannot open image")
        }
    }

    private fun openINaturalistSearch(scientificName: String) {
        try {
            val searchUrl = "https://www.inaturalist.org/search?q=${Uri.encode(scientificName)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                showError("No browser available to open iNaturalist")
            }
        } catch (e: Exception) {
            showError("Failed to open iNaturalist search")
        }
    }

    private fun sharePestInfo(pest: Pest) {
        try {
            val shareText = buildShareText(pest)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Pest Information: ${pest.name}")
                type = "text/plain"
            }

            val chooser = Intent.createChooser(shareIntent, "Share Pest Information")
            if (chooser.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(chooser)
            } else {
                showError("No apps available to share")
            }
        } catch (e: Exception) {
            showError("Failed to share pest information")
        }
    }

    private fun buildShareText(pest: Pest): String {
        return buildString {
            appendLine("🐛 ${pest.name ?: "Unknown Pest"}")
            appendLine("🔬 Scientific Name: ${pest.scientificName ?: "Not available"}")
            appendLine()
            appendLine("📋 Description:")
            appendLine(pest.description ?: "No description available")
            appendLine()
            appendLine("⚠️ Harm Level: ${pest.harmLevel?.displayName ?: "Unknown"}")
            appendLine("🌍 Regions: ${pest.regions?.joinToString(", ") ?: "No regions specified"}")
            appendLine("📊 Occurrence Count: ${pest.occurrenceCount ?: 0}")
            appendLine()
            appendLine("🔬 Biological Characteristics:")
            appendLine(pest.biologicalCharacteristics ?: "No biological characteristics available")
            appendLine()
            appendLine("🛡️ Control Methods:")
            appendLine(pest.controlMethods ?: "No control methods available")
            appendLine()
            if (!pest.pestInsecticide.isNullOrEmpty()) {
                appendLine("💊 Recommended Insecticides:")
                pest.pestInsecticide.forEach { insecticide ->
                    appendLine("• $insecticide")
                }
            }
            appendLine()
            appendLine("Shared from Pest Management App")
        }
    }

    private fun reportPestSighting(pest: Pest) {
        // TODO: Implement reporting functionality
        // This could navigate to a reporting form or send data to your API
        showInfo("Reporting feature coming soon!")
    }

    private fun getHelpForPest(pest: Pest) {
        // TODO: Implement help functionality
        // This could open FAQ, contact support, or pest management guide
        showInfo("Help feature coming soon!")
    }

    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.error_red))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }
    }

    private fun showInfo(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.primary))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        // Make dialog full width with proper margins
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}