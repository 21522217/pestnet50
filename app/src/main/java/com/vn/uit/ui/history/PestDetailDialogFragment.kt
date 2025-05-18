package com.vn.uit.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vn.uit.R
import com.vn.uit.ui.history.HistoryItem
import com.vn.uit.ui.history.PestItem

class PestDetailDialogFragment : DialogFragment() {

    private lateinit var closeButton: ImageButton
    private lateinit var detailImageView: ImageView
    private lateinit var pestNameTextView: TextView
    private lateinit var scientificNameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var occurrencesTextView: TextView
    private lateinit var detectedPestsRecyclerView: RecyclerView
    private lateinit var loadingView: View
    private lateinit var errorView: TextView

    private lateinit var imagesAdapter: HistoryAdapter
    private val pestImages = mutableListOf<HistoryItem>()

    private var pestId: String? = null

    // Use a factory or dependency injection in a real app
    private val viewModel: HistoryViewModel by viewModels({ requireActivity() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        pestId = arguments?.getString(ARG_PEST_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use the correct layout for pest details
        val root = inflater.inflate(R.layout.dialog_image_detail, container, false)

        // Initialize views with proper findViewById calls
        closeButton = root.findViewById(R.id.closeButton)
        detailImageView = root.findViewById(R.id.detailImageView)
        pestNameTextView = root.findViewById(R.id.pestNameTextView)
        scientificNameTextView = root.findViewById(R.id.scientificNameTextView)
        descriptionTextView = root.findViewById(R.id.descriptionTextView)
        occurrencesTextView = root.findViewById(R.id.occurrencesTextView)
        detectedPestsRecyclerView = root.findViewById(R.id.detectedPestsRecyclerView)
        loadingView = root.findViewById(R.id.loadingView)
        errorView = root.findViewById(R.id.errorView)

        setupAdapter()
        setupListeners()
        setupObservers()

        pestId?.let {
            viewModel.getPestDetails(it)
        } ?: run {
            errorView.text = "No pest ID provided"
            errorView.visibility = View.VISIBLE
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun setupAdapter() {
        imagesAdapter = HistoryAdapter(pestImages) { item ->
            // Show image detail when clicked
            val dialog = ImageDetailDialogFragment.newInstance(item.id)
            dialog.show(parentFragmentManager, "ImageDetailDialog")
        }
        detectedPestsRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        detectedPestsRecyclerView.adapter = imagesAdapter
    }

    private fun setupListeners() {
        closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { errorMessage ->
            if (errorMessage != null) {
                errorView.text = errorMessage
                errorView.visibility = View.VISIBLE
            } else {
                errorView.visibility = View.GONE
            }
        })

        viewModel.selectedPestDetails.observe(viewLifecycleOwner, Observer { (pestItem, images) ->
            updateUI(pestItem, images)
        })
    }

    private fun updateUI(pestItem: PestItem, images: List<HistoryItem>) {
        Glide.with(requireContext())
            .load(pestItem.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .into(detailImageView)

        pestNameTextView.text = pestItem.name
        scientificNameTextView.text = pestItem.scientificName
        descriptionTextView.text = pestItem.description
        occurrencesTextView.text = getString(R.string.pest_occurrences, pestItem.occurrenceCount)

        pestImages.clear()
        pestImages.addAll(images)
        imagesAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val ARG_PEST_ID = "arg_pest_id"

        fun newInstance(pestId: String): PestDetailDialogFragment {
            val fragment = PestDetailDialogFragment()
            val args = Bundle()
            args.putString(ARG_PEST_ID, pestId)
            fragment.arguments = args
            return fragment
        }
    }
}