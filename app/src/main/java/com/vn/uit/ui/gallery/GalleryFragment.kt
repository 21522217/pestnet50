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


    private val pestClassificationViewModel: PestClassificationViewModel by activityViewModels()


    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val imageUri = result.data?.data


            imageUri?.let {

                pestClassificationViewModel.setImageUri(it)


                val bundle = Bundle().apply {
                    putString("imageUri", it.toString())
                }


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

        setupRecyclerView()

        binding.pickImageButton.setOnClickListener {
            openGalleryPicker()
        }
    }

    private fun setupRecyclerView() {

        binding.recyclerGallery.layoutManager = GridLayoutManager(requireContext(), 3)


        galleryViewModel.loadGalleryImages()


        val galleryAdapter = GalleryAdapter { imageUri ->
            navigateToImageEditor(imageUri)
        }
        binding.recyclerGallery.adapter = galleryAdapter


        galleryViewModel.galleryImages.observe(viewLifecycleOwner) { images ->

            galleryAdapter.submitList(images)


            binding.emptyGalleryText.visibility = if (images.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openGalleryPicker() {

        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun navigateToImageEditor(imageUri: Uri) {

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