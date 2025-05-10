package com.vn.uit.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.FragmentLoginBinding
import com.vn.uit.datastore.UserPreferences
import com.vn.uit.model.AuthRequest
import kotlinx.coroutines.launch

/**
 * LoginFragment - Handles user authentication
 * This fragment works with MainActivity's navigation system which automatically
 * handles navigation between auth and main graphs based on token availability
 */
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        // Clear previous errors
        binding.usernameEditText.error = null
        binding.passwordEditText.error = null

        if (email.isEmpty()) {
            binding.usernameEditText.error = "Email cannot be empty"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password cannot be empty"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        // Show loading indicator
        toggleLoadingState(true)

        lifecycleScope.launch {
            try {
                val response = ApiClient.authApi.login(AuthRequest(email, password))

                if (response.success && response.data != null) {
                    // Save user data - MainActivity will handle the navigation
                    userPreferences.saveToken(response.data.token)
                    userPreferences.saveUserId(response.data.userId)

                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()

                    // No need to explicitly navigate - MainActivity's token collector will handle it
                } else {
                    // Handle authentication failure
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleLoginError(e)
            } finally {
                // Hide loading indicator
                toggleLoadingState(false)
            }
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            loginButton.isEnabled = !isLoading
            usernameEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
        }
    }

    private fun handleLoginError(error: Exception) {
        val errorMessage = when {
            error.message?.contains("timeout") == true ->
                "Connection timeout. Please try again."
            error.message?.contains("Unable to resolve host") == true ->
                "No internet connection."
            else -> "Login failed: ${error.localizedMessage ?: "Unknown error"}"
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
