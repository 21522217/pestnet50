package com.vn.uit.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.databinding.FragmentSignupBinding
import com.vn.uit.datastore.UserPreferences
import com.vn.uit.model.AuthData
import com.vn.uit.model.AuthResponse
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Main signup button
        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInputs(email, username, password, confirmPassword)) {
                performSignup(email, username, password)
            }
        }

        // Already have an account link
        binding.loginTextView.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }

        // Terms and Privacy
        binding.termsTextView.setOnClickListener {
            Toast.makeText(requireContext(), "Terms and Privacy not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(email: String, username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Clear previous errors
        binding.emailEditText.error = null
        binding.usernameEditText.error = null
        binding.passwordEditText.error = null
        binding.confirmPasswordEditText.error = null

        if (email.isEmpty()) {
            binding.emailEditText.error = "Email cannot be empty"
            isValid = false
        }

        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username cannot be empty"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password cannot be empty"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordEditText.error = "Please confirm your password"
            isValid = false
        }

        if (password != confirmPassword) {
            binding.confirmPasswordEditText.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun performSignup(email: String, username: String, password: String) {
        // Show loading indicator
        toggleLoadingState(true)

        lifecycleScope.launch {
            try {
                // Simulate a brief loading delay
                kotlinx.coroutines.delay(1000)

                // Create a fake/mock response
                val response = if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    AuthResponse(
                        success = true,
                        message = "Mock signup successful for $email",
                        data = AuthData(
                            token = "mock_jwt_token_$email",
                            userId = 456L
                        )
                    )
                } else {
                    AuthResponse(
                        success = false,
                        message = "Invalid information",
                        data = null
                    )
                }

                if (response.success && response.data != null) {
                    // Save user data to preferences
                    userPreferences.saveUserData(response.data.token, response.data.userId)

                    // Show success message
                    Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show()

                    // Let MainActivity handle the navigation
                    // We don't need to perform any navigation here
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleSignupError(e)
            } finally {
                toggleLoadingState(false)
            }
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        _binding?.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            signupButton.isEnabled = !isLoading
            emailEditText.isEnabled = !isLoading
            usernameEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            confirmPasswordEditText.isEnabled = !isLoading
            loginTextView.isEnabled = !isLoading
        }
    }

    private fun handleSignupError(error: Exception) {
        val errorMessage = when {
            error.message?.contains("timeout") == true ->
                "Connection timeout. Please try again."
            error.message?.contains("Unable to resolve host") == true ->
                "No internet connection to this device."
            else -> "Signup failed: ${error.localizedMessage ?: "Unknown error"}"
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}