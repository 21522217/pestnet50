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
import com.vn.uit.databinding.FragmentLoginBinding
import com.vn.uit.datastore.UserPreferences
import com.vn.uit.model.AuthData
import com.vn.uit.model.AuthResponse
import kotlinx.coroutines.launch

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
        // Main login button
        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        // Social login buttons
        binding.googleButton.setOnClickListener {
            Toast.makeText(requireContext(), "Google login not implemented yet", Toast.LENGTH_SHORT).show()
        }

        binding.facebookButton.setOnClickListener {
            Toast.makeText(requireContext(), "Facebook login not implemented yet", Toast.LENGTH_SHORT).show()
        }

        binding.othersButton.setOnClickListener {
            Toast.makeText(requireContext(), "Other login options not implemented yet", Toast.LENGTH_SHORT).show()
        }

        // Forgot password
        binding.forgotPasswordTextView.setOnClickListener {
            Toast.makeText(requireContext(), "Forgot password not implemented yet", Toast.LENGTH_SHORT).show()
        }

        // Terms and Privacy
        binding.termsTextView.setOnClickListener {
            Toast.makeText(requireContext(), "Terms and Privacy not implemented yet", Toast.LENGTH_SHORT).show()
        }

        binding.dontHaveAnAccount.setOnClickListener({
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        })
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
                // Commented out real API call
                // val response = ApiClient.authApi.login(AuthRequest(email, password))

                // Simulate a brief loading delay
                kotlinx.coroutines.delay(1000)

                // Create a fake/mock response instead
                val response = if (email.isNotEmpty() && password.isNotEmpty()) {
                    AuthResponse(
                        success = true,
                        message = "Mock login successful for $email",
                        data = AuthData(
                            token = "mock_jwt_token_$email",
                            userId = 123L
                        )
                    )
                } else {
                    AuthResponse(
                        success = false,
                        message = "Invalid credentials",
                        data = null
                    )
                }

                if (response.success && response.data != null) {
                    // Save remember me preference if checked
                    if (binding.rememberMeCheckBox.isChecked) {
                        // Save username for future login
                        userPreferences.saveUsername(email)
                    }

                    // Use single transaction to save user data
                    userPreferences.saveUserData(response.data.token, response.data.userId)

                    // Show success message
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()

                    // IMPORTANT: Let MainActivity handle the navigation
                    // We don't need to perform any navigation here
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                handleLoginError(e)
            } finally {
                toggleLoadingState(false)
            }
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        _binding?.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            loginButton.isEnabled = !isLoading
            googleButton.isEnabled = !isLoading
            facebookButton.isEnabled = !isLoading
            othersButton.isEnabled = !isLoading
            usernameEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            rememberMeCheckBox.isEnabled = !isLoading
            forgotPasswordTextView.isEnabled = !isLoading
        }
    }

    private fun handleLoginError(error: Exception) {
        val errorMessage = when {
            error.message?.contains("timeout") == true ->
                "Connection timeout. Please try again."
            error.message?.contains("Unable to resolve host") == true ->
                "No internet connection to this device."
            else -> "Login failed: ${error.localizedMessage ?: "Unknown error"}"
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}