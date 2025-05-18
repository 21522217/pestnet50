package com.vn.uit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vn.uit.R
import com.vn.uit.ui.history.DetectedPestAdapter
import com.vn.uit.ui.history.DetectedPestItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ImageDetailDialogFragment : DialogFragment() {

    private lateinit var closeButton: ImageButton
    private lateinit var detailImageView: ImageView
    private lateinit var detectedPestsRecyclerView: RecyclerView
    private lateinit var uploadDateTextView: TextView
    private lateinit var fileNameTextView: TextView

    private lateinit var detectedPestAdapter: DetectedPestAdapter
    private val detectedPests = mutableListOf<DetectedPestItem>()

    private var imageId: String? = null
    private val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        imageId = arguments?.getString(ARG_IMAGE_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.dialog_image_detail, container, false)

        // Initialize views
        closeButton = root.findViewById(R.id.closeButton)
        detailImageView = root.findViewById(R.id.detailImageView)
        detectedPestsRecyclerView = root.findViewById(R.id.detectedPestsRecyclerView)
        uploadDateTextView = root.findViewById(R.id.uploadDateTextView)
        fileNameTextView = root.findViewById(R.id.fileNameTextView)

        setupAdapter()
        setupListeners()
        loadImageDetails()

        return root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setupAdapter() {
        detectedPestAdapter = DetectedPestAdapter(detectedPests)
        detectedPestsRecyclerView.adapter = detectedPestAdapter
    }

    private fun setupListeners() {
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun loadImageDetails() {
        // In a real app, you would fetch this data from your API or database
        // For this example, we'll use mock data

        // Mock image details
        val imageUrl = "https://placekitten.com/500/500"
        val uploadDate = Date()
        val fileName = "plant_image_${imageId?.substring(0, 4)}.jpg"

        // Set image
        Glide.with(requireContext())
            .load(imageUrl)
            .centerCrop()
            .placeholder(android.R.color.darker_gray)
            .into(detailImageView)

        // Set date and file name
        uploadDateTextView.text = "Uploaded: ${dateFormat.format(uploadDate)}"
        fileNameTextView.text = "File: $fileName"

        // Mock detected pests
        detectedPests.addAll(
            listOf(
                DetectedPestItem(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    "Aphid",
                    "Aphidoidea",
                    "https://placekitten.com/100/100",
                    0.95f
                ),
                DetectedPestItem(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    "Whitefly",
                    "Aleyrodidae",
                    "https://placekitten.com/101/101",
                    0.82f
                ),
                DetectedPestItem(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    "Thrips",
                    "Thysanoptera",
                    "https://placekitten.com/102/102",
                    0.67f
                )
            )
        )
        detectedPestAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val ARG_IMAGE_ID = "arg_image_id"

        fun newInstance(imageId: String): ImageDetailDialogFragment {
            val fragment = ImageDetailDialogFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_ID, imageId)
            fragment.arguments = args
            return fragment
        }
    }
}