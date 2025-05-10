package com.vn.uit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.databinding.FragmentResultBinding
import com.vn.uit.model.PestInfo
import com.vn.uit.repository.PestInfoRepository
import com.vn.uit.viewmodel.PestClassificationViewModel

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PestClassificationViewModel by activityViewModels()
    private val pestInfoRepository by lazy {
        PestInfoRepository(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get pest name from ViewModel
        viewModel.detectedPestName.observe(viewLifecycleOwner) { pestName ->
            if (pestName != null) {
                displayPestInfo(pestName)
            } else {
                binding.pestNameTextView.text = "Unknown Pest"
                binding.descriptionTextView.text = "No pest detected or classification failed."
                binding.treatmentTextView.text = "Please try again with a clearer image."
            }
        }

        // Display the processed image
        viewModel.processedImage.observe(viewLifecycleOwner) { bitmap ->
            if (bitmap != null) {
                binding.resultImageView.setImageBitmap(bitmap)
            } else {
                binding.resultImageView.setImageResource(R.drawable.placeholder_image)
            }
        }

        binding.newScanButton.setOnClickListener {
            viewModel.resetClassification()
            findNavController().popBackStack()
            findNavController().popBackStack()
        }

        binding.saveResultButton.setOnClickListener {
            viewModel.saveResult()?.let { uri ->
                Toast.makeText(
                    requireContext(),
                    "Result saved to gallery: ${uri.lastPathSegment}",
                    Toast.LENGTH_SHORT
                ).show()
            } ?: Toast.makeText(
                requireContext(),
                "Failed to save result",
                Toast.LENGTH_SHORT
            ).show()

            viewModel.resetClassification()
            findNavController().popBackStack(R.id.cameraFragment, false)
        }
    }

    private fun displayPestInfo(pestName: String) {
        val pestInfo = pestInfoRepository.getPestInfo(pestName)
        binding.pestNameTextView.text = pestInfo.name
        binding.descriptionTextView.text = pestInfo.description
        binding.treatmentTextView.text = pestInfo.treatmentOptions
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
