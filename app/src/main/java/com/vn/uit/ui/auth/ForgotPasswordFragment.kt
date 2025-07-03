package com.vn.uit.ui.auth

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.FragmentForgotPasswordBinding
import com.vn.uit.model.ForgotPasswordRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private var resetPasswordJob: Job? = null
    private val TAG = "ForgotPasswordFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            if (validateEmail(email)) {
                performPasswordReset(email)
            }
        }

        binding.contactSupportButton.setOnClickListener {
            safeShowToast("Contact support not implemented yet")
        }

        binding.backToLoginTextView.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateEmail(email: String): Boolean {
        binding.emailEditText.error = null
        hideMessage()

        return when {
            email.isEmpty() -> {
                binding.emailEditText.error = "Email cannot be empty"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = "Invalid email format"
                false
            }
            else -> true
        }
    }

    private fun performPasswordReset(email: String) {
        resetPasswordJob?.cancel()
        toggleLoadingState(true)
        hideMessage()

        resetPasswordJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(1000) // Simulate network delay
                if (!isAdded) return@launch

                val response = ApiClient.authApi.forgotPassword(ForgotPasswordRequest("", email))
                if (!isAdded) return@launch

                if (response.status == 200) {
                    showSuccessMessage(getString(R.string.reset_email_sent_success))
                    disableFormTemporarily()
                } else {
                    val errorMessage = response.message ?: "Failed to send reset email"
                    showErrorMessage(errorMessage)
                }
            } catch (e: Exception) {
                handlePasswordResetError(e)
            } finally {
                toggleLoadingState(false)
            }
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            binding.resetPasswordButton.isEnabled = !isLoading
            binding.emailEditText.isEnabled = !isLoading
            binding.backButton.isEnabled = !isLoading
            binding.contactSupportButton.isEnabled = !isLoading
            binding.backToLoginTextView.isEnabled = !isLoading
        }
    }

    private fun handlePasswordResetError(error: Exception) {
        if (!isAdded) return

        val errorMessage = when {
            error is HttpException -> {
                val raw = error.response()?.errorBody()?.string()
                parseErrorMessage(raw)
            }
            error.message?.contains("timeout", true) == true -> "Connection timeout. Please try again."
            error.message?.contains("Unable to resolve host", true) == true -> "No internet connection. Please check your network."
            else -> "Failed to send reset email: ${error.localizedMessage ?: "Unknown error"}"
        }

        Log.e(TAG, "Password reset error", error)
        showErrorMessage(errorMessage)
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = JSONObject(errorBody ?: return "Unknown error")
            json.optString("message", "Unknown error")
        } catch (e: Exception) {
            "Failed to parse error response"
        }
    }


    private fun showSuccessMessage(message: String) {
        _binding?.let { binding ->
            binding.messageTextView.apply {
                text = message
                setTextColor(ContextCompat.getColor(requireContext(), R.color.risk_low))
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background))
                visibility = View.VISIBLE
            }
        }
    }

    private fun showErrorMessage(message: String) {
        _binding?.let { binding ->
            binding.messageTextView.apply {
                text = message
                setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.editTextBackground))
                visibility = View.VISIBLE
            }
        }
    }

    private fun hideMessage() {
        _binding?.messageTextView?.visibility = View.GONE
    }

    private fun disableFormTemporarily() {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.resetPasswordButton.isEnabled = false
            binding.emailEditText.isEnabled = false

            // Re-enable after 30 seconds to prevent spam
            delay(30000)

            if (isAdded && _binding != null) {
                binding.resetPasswordButton.isEnabled = true
                binding.emailEditText.isEnabled = true
            }
        }
    }

    private fun safeShowToast(message: String) {
        context?.takeIf { isAdded }?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        resetPasswordJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}