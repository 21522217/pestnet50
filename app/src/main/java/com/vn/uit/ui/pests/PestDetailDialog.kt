package com.vn.uit.ui.pests

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.card.MaterialCardView
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
    ) = PestDetailDialogBinding.inflate(inflater, container, false).also {
        _binding = it
    }.root

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pest = requireArguments().getParcelable<Pest>(ARG_PEST) ?: return

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
            pestName.text = pest.name ?: "Unknown Pest"
            pestScientificName.text = pest.scientificName ?: "Scientific name not available"
            pestDescription.text = pest.description ?: "No description available"

            val imageUrl = pest.relatedImages.firstOrNull()
            if (imageUrl != null) {
                pestImage.load(imageUrl) {
                    placeholder(R.drawable.placeholder_image)
                    error(R.drawable.image_placeholder)
                }
            } else {
                pestImage.load(R.drawable.placeholder_image)
            }
        }
    }


    private fun setupHarmLevelBadge(pest: Pest) {
        binding.harmLevelBadge.apply {
            val harmLevel = pest.harmLevel

            text = harmLevel?.let { "${it.displayName.uppercase()} RISK" } ?: "UNKNOWN RISK"

            val badgeColor = harmLevel?.let { level ->
                when (level) {
                    HarmLevel.VERY_HIGH, HarmLevel.HIGH -> R.color.risk_high
                    HarmLevel.MODERATE -> R.color.risk_medium
                    HarmLevel.LOW, HarmLevel.VERY_LOW -> R.color.risk_low
                }
            } ?: R.color.risk_medium

            setBackgroundColor(ContextCompat.getColor(requireContext(), badgeColor))
        }
    }

    private fun setupOccurrenceCount(pest: Pest) {
        val occurrenceCount = (pest.occurrenceCount ?: 0).toString()
        binding.apply {
            this.occurrenceCount.text = occurrenceCount
            occurrenceCountDetail.text = occurrenceCount
        }
    }

    private fun setupRegionsAffected(pest: Pest) {
        val regions = pest.regions ?: emptyList()
        val regionsText = regions.takeIf { it.isNotEmpty() }
            ?.joinToString(", ")
            ?: "No regions specified"

        TextView(requireContext()).apply {
            text = regionsText
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        }
    }

    private fun setupBiologicalCharacteristics(pest: Pest) {
        binding.biologicalCharacteristics.text = pest.biologicalCharacteristics
            ?: "No biological characteristics available"
    }

    private fun setupControlMethods(pest: Pest) {
        binding.controlMethods.text = pest.controlMethods
            ?: "No control methods available"
    }

    private fun setupInsecticides(pest: Pest) {
        binding.insecticidesContainer.apply {
            removeAllViews()

            val insecticides = pest.pestInsecticide ?: emptyList()
            val items = insecticides.takeIf { it.isNotEmpty() }
                ?: listOf("No insecticides specified")

            items.forEach { insecticide ->
                addView(createInsecticideCard(insecticide))
            }
        }
    }

    private fun createInsecticideCard(insecticideName: String): MaterialCardView {
        return MaterialCardView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 12
            }
            cardElevation = 4f
            radius = 8f
            useCompatPadding = true

            addView(TextView(requireContext()).apply {
                text = insecticideName
                setPadding(16, 12, 16, 12)
                textSize = 12f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_dark))
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.insecticide_background))
            })
        }
    }

    private fun setupRelatedImages(pest: Pest) {
        val relatedImages = pest.relatedImages
        if (relatedImages.isNotEmpty()) {
            val adapter = RelatedImagesAdapter(relatedImages) { imageUrl ->
                openImageFullScreen(imageUrl)
            }

            binding.relatedImagesRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                this.adapter = adapter
            }
        }
    }

    private fun setupStatistics(pest: Pest) {
        binding.apply {
            occurrenceCountDetail.text = (pest.occurrenceCount ?: 0).toString()

            harmLevelText.apply {
                val harmLevel = pest.harmLevel
                text = harmLevel?.displayName?.uppercase() ?: "UNKNOWN"

                val harmColor = harmLevel?.let { level ->
                    when (level) {
                        HarmLevel.VERY_HIGH, HarmLevel.HIGH -> R.color.risk_high
                        HarmLevel.MODERATE -> R.color.risk_medium
                        HarmLevel.LOW, HarmLevel.VERY_LOW -> R.color.risk_low
                    }
                } ?: R.color.risk_medium

                setTextColor(ContextCompat.getColor(requireContext(), harmColor))
            }

            regionsCount.text = (pest.regions?.size ?: 0).toString()
        }
    }

    private fun setupActionButtons(pest: Pest) {
        binding.apply {
            btnViewOnINaturalist.setOnClickListener {
                val scientificName = pest.scientificName ?: pest.name ?: "unknown"
                openINaturalistSearch(scientificName)
            }

            btnShare.setOnClickListener {
                sharePestInfo(pest)
            }

            btnReportSighting.setOnClickListener {
                reportPestSighting(pest)
            }

            btnGetHelp.setOnClickListener {
                getHelpForPest(pest)
            }
        }
    }

    private fun openImageFullScreen(imageUrl: String) {
        // TODO: Implement full screen image viewer
    }

    private fun openINaturalistSearch(scientificName: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.inaturalist.org/search?q=$scientificName"))
            startActivity(intent)
        }
    }

    private fun sharePestInfo(pest: Pest) {
        val shareText = buildString {
            appendLine("Found ${pest.name ?: "Unknown Pest"} (${pest.scientificName ?: "Scientific name not available"})")
            appendLine()
            appendLine(pest.description ?: "No description available")
            appendLine()
            appendLine("Harm Level: ${pest.harmLevel?.displayName ?: "Unknown"}")
            appendLine("Regions: ${pest.regions?.joinToString(", ") ?: "No regions specified"}")
            appendLine()
            append("Control Methods: ${pest.controlMethods ?: "No control methods available"}")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Pest Info"))
    }

    private fun reportPestSighting(pest: Pest) {
        // TODO: Navigate to report form or make API call
    }

    private fun getHelpForPest(pest: Pest) {
        // TODO: Open FAQ, contact support, or pest management guide
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}