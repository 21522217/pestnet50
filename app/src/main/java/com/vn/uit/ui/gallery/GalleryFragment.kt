package com.vn.uit.ui.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.vn.uit.R
import com.vn.uit.databinding.FragmentGalleryBinding
import com.vn.uit.viewmodel.PestClassificationViewModel

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryViewModel: GalleryViewModel

    // Share the PestClassificationViewModel with ImageEditorFragment
    private val pestClassificationViewModel: PestClassificationViewModel by activityViewModels()

    // Activity result launcher for picking images
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Get selected image Uri
            val imageUri = result.data?.data

            // Process the image with pest classification before navigating
            imageUri?.let {
                // Set the image in the shared ViewModel
                pestClassificationViewModel.setImageUri(it)

                // Navigate to image editor with the selected image
                val bundle = Bundle().apply {
                    putString("imageUri", it.toString())
                }

                // Navigate to image editor
                findNavController().navigate(
                    R.id.action_galleryFragment_to_imageEditorFragment,
                    bundle
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        galleryViewModel = ViewModelProvider(this)[GalleryViewModel::class.java]

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup UI and listeners
        setupRecyclerView()

        // If navigated directly from Home for image selection, show image picker immediately
        if (findNavController().previousBackStackEntry?.destination?.id == R.id.nav_home) {
            openGalleryPicker()
        }

        // Setup gallery picker button
        binding.pickImageButton.setOnClickListener {
            openGalleryPicker()
        }
    }

    private fun setupRecyclerView() {
        // Set up the RecyclerView with a GridLayoutManager
        binding.recyclerGallery.layoutManager = GridLayoutManager(requireContext(), 3)

        // Load gallery images from ViewModel
        galleryViewModel.loadGalleryImages()

        // Create and set adapter
        val galleryAdapter = GalleryAdapter { imageUri ->
            navigateToImageEditor(imageUri)
        }
        binding.recyclerGallery.adapter = galleryAdapter

        // Observe gallery images
        galleryViewModel.galleryImages.observe(viewLifecycleOwner) { images ->
            // Update adapter with images
            galleryAdapter.submitList(images)

            // Show/hide empty state
            binding.emptyGalleryText.visibility = if (images.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openGalleryPicker() {
        // Create intent to pick image from gallery
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun navigateToImageEditor(imageUri: Uri) {
        // Process the image with pest classification before navigating
        pestClassificationViewModel.setImageUri(imageUri)

        val bundle = Bundle().apply {
            putString("imageUri", imageUri.toString())
        }
        findNavController().navigate(
            R.id.action_galleryFragment_to_imageEditorFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}