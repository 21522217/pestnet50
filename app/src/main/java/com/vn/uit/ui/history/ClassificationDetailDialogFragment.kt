package com.vn.uit.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.vn.uit.R
import com.vn.uit.model.ClassificationResponse
import com.vn.uit.ui.history.ClassificationAdapter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClassificationDetailDialogFragment(
    private val classification: ClassificationResponse,
    private val relatedImages: List<String> = emptyList(),
    private val onShare: (() -> Unit)? = null,
    private val onRemove: (() -> Unit)? = null
) : DialogFragment() {

    private lateinit var imageViewClassified: ImageView
    private lateinit var accuracyTag: TextView
    private lateinit var textViewPestName: TextView
    private lateinit var textViewScientificName: TextView
    private lateinit var textViewModelName: TextView
    private lateinit var textViewConfidence: TextView
    private lateinit var textViewClassifiedAt: TextView
    private lateinit var textViewPestRegions: TextView
    private lateinit var textViewPestDescription: TextView
    private lateinit var textViewInsecticideTitle: TextView
    private lateinit var textViewInsecticideList: TextView
    private lateinit var textViewPestUrl: TextView
    private lateinit var relatedImagesRecycler: RecyclerView
    private lateinit var btnClose: ImageButton
    private lateinit var btnShare: Button
    private lateinit var btnRemove: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.BlurDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.classification_detail_dialog, null)
        dialog.setContentView(view)

        val metrics = resources.displayMetrics
        val topMarginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, metrics).toInt()
        val screenHeight = metrics.heightPixels
        val dialogHeight = screenHeight - topMarginPx

        dialog.window?.let { window ->
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight)
            window.setGravity(Gravity.BOTTOM)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setDimAmount(0.6f)
            window.setWindowAnimations(R.style.BottomDialogAnimation)
        }

        dialog.setCanceledOnTouchOutside(true)

        bindViews(view)
        setupViews()
        setupListeners()

        return dialog
    }

    private fun bindViews(view: View) {
        imageViewClassified = view.findViewById(R.id.imageViewClassified)
        accuracyTag = view.findViewById(R.id.accuracyTag)
        textViewPestName = view.findViewById(R.id.textViewPestName)
        textViewScientificName = view.findViewById(R.id.textViewScientificName)
        textViewModelName = view.findViewById(R.id.textViewModelName)
        textViewConfidence = view.findViewById(R.id.textViewConfidence)
        textViewClassifiedAt = view.findViewById(R.id.textViewClassifiedAt)
        textViewPestRegions = view.findViewById(R.id.textViewPestRegions)
        textViewPestDescription = view.findViewById(R.id.textViewPestDescription)
        textViewInsecticideTitle = view.findViewById(R.id.textViewInsecticideTitle)
        textViewInsecticideList = view.findViewById(R.id.textViewInsecticideList)
        textViewPestUrl = view.findViewById(R.id.textViewPestUrl)
        relatedImagesRecycler = view.findViewById(R.id.relatedImagesRecycler)
        btnClose = view.findViewById(R.id.btnClose)
        btnShare = view.findViewById(R.id.btnShare)
        btnRemove = view.findViewById(R.id.btnRemove)
    }

    private fun setupViews() {
        val imageUrl = classification.imageUrl.ifBlank {
            classification.pestUrl ?: ""
        }
        imageViewClassified.load(imageUrl) {
            crossfade(true)
            placeholder(R.drawable.image_placeholder)
            error(R.drawable.image_broken)
        }

        accuracyTag.text = "${"%.1f".format(classification.confidence * 100)}%"

        textViewPestName.text = classification.pestName
        textViewScientificName.text = classification.pestScientificName ?: "N/A"
        textViewModelName.text = classification.modelName
        textViewConfidence.text = "Confidence: ${"%.1f".format(classification.confidence * 100)}%"

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            .withZone(ZoneId.systemDefault())
        textViewClassifiedAt.text = "Classified at: ${classification.classifiedAt}}"

        val regionsText = classification.pestRegions?.joinToString(", ") ?: "Unknown regions"
        textViewPestRegions.text = "Regions: $regionsText"

        textViewPestDescription.text = classification.pestDescription ?: "No description available."

        if (!classification.pestInsecticide.isNullOrEmpty()) {
            textViewInsecticideTitle.isVisible = true
            textViewInsecticideList.isVisible = true
            textViewInsecticideList.text = classification.pestInsecticide.joinToString(", ")
        } else {
            textViewInsecticideTitle.isVisible = false
            textViewInsecticideList.isVisible = false
        }

        if (!classification.pestUrl.isNullOrBlank()) {
            textViewPestUrl.isVisible = true
            textViewPestUrl.text = classification.pestUrl
            textViewPestUrl.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(classification.pestUrl))
                startActivity(intent)
            }
        } else {
            textViewPestUrl.isVisible = false
        }

        if (relatedImages.isNotEmpty()) {
            relatedImagesRecycler.isVisible = true
            relatedImagesRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            val relatedClassifications = relatedImages.map {
                ClassificationResponse(
                    imageUrl = it,
                    confidence = 0f,
                    modelName = "",
                    classifiedAt = classification.classifiedAt,
                    pestId = classification.pestId,
                    pestName = "",
                    pestRegions = null,
                    pestScientificName = "",
                    pestDescription = null,
                    pestUrl = null,
                    pestInsecticide = null
                )
            }
            val adapter = ClassificationAdapter(relatedClassifications) {
                Toast.makeText(requireContext(), "Clicked related image", Toast.LENGTH_SHORT).show()
            }
            relatedImagesRecycler.adapter = adapter
        } else {
            relatedImagesRecycler.isVisible = false
        }
    }

    private fun setupListeners() {
        btnClose.setOnClickListener {
            dismiss()
        }

        btnShare.setOnClickListener {
            onShare?.invoke() ?: Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show()
        }

        btnRemove.setOnClickListener {
            onRemove?.invoke() ?: Toast.makeText(requireContext(), "Remove clicked", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}
