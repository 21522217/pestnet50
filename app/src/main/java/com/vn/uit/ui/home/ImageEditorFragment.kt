package com.vn.uit.ui.home

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.databinding.FragmentImageEditorBinding
import com.vn.uit.viewmodel.PestClassificationViewModel
import androidx.lifecycle.Observer

class ImageEditorFragment : Fragment() {

    private var _binding: FragmentImageEditorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PestClassificationViewModel by activityViewModels()

    private val classificationObserver = Observer<PestClassificationViewModel.ClassificationState> { state ->
        when (state) {
            is PestClassificationViewModel.ClassificationState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            is PestClassificationViewModel.ClassificationState.Success -> {
                binding.progressBar.visibility = View.GONE
                findNavController().navigate(
                    R.id.action_imageEditorFragment_to_resultFragment
                )
            }
            is PestClassificationViewModel.ClassificationState.Error -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${state.message}", Toast.LENGTH_SHORT).show()
            }
            else -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI state
        binding.progressBar.visibility = View.GONE

        // Observe processed image and set to cropper
        viewModel.processedImage.observe(viewLifecycleOwner) { bitmap ->
            if (bitmap != null) {
                binding.imageCropper.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "No image available", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        binding.saveButton.setOnClickListener {
            try {
                val croppedBitmap = binding.imageCropper.croppedImage
                if (croppedBitmap != null) {
                    // Store the cropped image before classification
                    viewModel.setProcessedImage(croppedBitmap)
                    classifyImage(croppedBitmap)
                } else {
                    Toast.makeText(requireContext(), "Failed to crop image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error cropping image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelButton.setOnClickListener {
            viewModel.resetClassification()
            findNavController().popBackStack()
        }
    }

    private fun classifyImage(croppedBitmap: Bitmap) {
        binding.progressBar.visibility = View.VISIBLE

        // Register the observer
        viewModel.classificationState.observe(viewLifecycleOwner, classificationObserver)

        // Start classification
        viewModel.classifyImage(croppedBitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
