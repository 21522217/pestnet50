package com.vn.uit.ui.home

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.databinding.FragmentImageEditorBinding
import com.vn.uit.viewmodel.PestClassificationViewModel

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
                // Add null check for the result
                state.result?.let { result ->
                    val action = ImageEditorFragmentDirections.actionImageEditorFragmentToResultFragment(result)
                    findNavController().navigate(action)
                }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.GONE

        viewModel.processedImage.observe(viewLifecycleOwner) { bitmap ->
            if (bitmap != null) {
                binding.imageCropper.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), R.string.no_images_found, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        binding.saveButton.setOnClickListener {
            val croppedBitmap = binding.imageCropper.croppedImage
            if (croppedBitmap != null) {
                viewModel.setProcessedImage(croppedBitmap)
                classifyImage(croppedBitmap)
            } else {
                Toast.makeText(requireContext(), R.string.failed_to_open_inaturalist, Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelButton.setOnClickListener {
            viewModel.resetClassification()
            findNavController().popBackStack()
        }
    }

    private fun classifyImage(croppedBitmap: Bitmap) {
        binding.progressBar.visibility = View.VISIBLE
        // Remove the previous observer before adding a new one to avoid multiple observers
        viewModel.classificationState.removeObserver(classificationObserver)
        viewModel.classificationState.observe(viewLifecycleOwner, classificationObserver)
        viewModel.classifyImage(croppedBitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up observer
        viewModel.classificationState.removeObserver(classificationObserver)
        _binding = null
    }
}