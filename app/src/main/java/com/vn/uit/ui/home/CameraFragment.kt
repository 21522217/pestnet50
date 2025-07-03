package com.vn.uit.ui.home

import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.databinding.FragmentCameraBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.fragment.app.activityViewModels
import com.vn.uit.viewmodel.PestClassificationViewModel

class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val viewModel: PestClassificationViewModel by activityViewModels()
    private val binding get() = _binding!!
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var flashEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Clear any previous state when entering the camera fragment
        viewModel.resetClassification()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Setup click listeners first
        setupClickListeners()

        // Check permissions and start camera
        checkPermissionsAndStartCamera()
    }

    private fun setupClickListeners() {
        binding.captureButton.setOnClickListener {
            takePhoto()
        }

        binding.flashButton.setOnClickListener {
            toggleFlash()
        }

        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.galleryButton.setOnClickListener {
            viewModel.resetClassification()
            findNavController().navigate(R.id.action_cameraFragment_to_galleryFragment)
        }
    }

    private fun checkPermissionsAndStartCamera() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun toggleFlash() {
        camera?.let {
            flashEnabled = !flashEnabled
            it.cameraControl.enableTorch(flashEnabled)
            binding.flashButton.setImageResource(
                if (flashEnabled) R.drawable.ic_flash_on else R.drawable.ic_flash_off
            )
        }
    }

    private fun startCamera() {
        // Add a small delay to ensure the view is fully ready
        binding.previewView.post {
            initializeCamera()
        }
    }

    private fun initializeCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                // Check if fragment is still attached
                if (!isAdded) {
                    Log.w(TAG, "Fragment not attached, skipping camera initialization")
                    return@addListener
                }

                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

                // Initialize flash button based on torch availability
                camera?.cameraInfo?.hasFlashUnit()?.let { hasFlash ->
                    binding.flashButton.visibility = if (hasFlash) View.VISIBLE else View.GONE
                }

                Log.d(TAG, "Camera initialized successfully")

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(requireContext(), "Failed to start camera: ${exc.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Show loading indicator or disable capture button
        binding.captureButton.isEnabled = false

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()), // Use main executor instead of custom executor
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    if (savedUri != null) {
                        Log.d(TAG, "Photo capture succeeded: $savedUri")
                        // Store URI in ViewModel
                        viewModel.setImageUri(savedUri)
                        findNavController().navigate(R.id.action_cameraFragment_to_imageEditorFragment)
                    } else {
                        Log.e(TAG, "Photo capture succeeded but null URI returned")
                        Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                    binding.captureButton.isEnabled = true
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(
                        requireContext(),
                        "Capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.captureButton.isEnabled = true
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted, starting camera")
                // Add a delay to ensure the permission dialog is fully dismissed
                binding.previewView.postDelayed({
                    if (isAdded && _binding != null) {
                        startCamera()
                    }
                }, 100)
            } else {
                Log.w(TAG, "Camera permission denied")
                Toast.makeText(requireContext(), "Camera permission is required to use the camera", Toast.LENGTH_LONG).show()
                // Consider navigating back or to another fragment that doesn't require camera
                findNavController().popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Restart camera if permission was granted while fragment was paused
        if (allPermissionsGranted() && camera == null && _binding != null) {
            Log.d(TAG, "onResume: restarting camera")
            startCamera()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        camera = null
        imageCapture = null
        _binding = null
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 10
    }
}