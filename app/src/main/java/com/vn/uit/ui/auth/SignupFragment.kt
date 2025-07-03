package com.vn.uit.ui.auth

import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.vn.uit.R
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.FragmentSignupBinding
import com.vn.uit.datastore.UserPreferences
import com.vn.uit.model.SignupRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private var signupJob: Job? = null
    private val TAG = "SignupFragment"
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

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
        setupClickListeners()
        setupPasswordToggles()
        initializePasswordFields()
    }

    private fun initializePasswordFields() {
        // Set initial eye icons for both password fields
        binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
        binding.confirmPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
    }

    private fun setupClickListeners() {
        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInputs(email, username, password, confirmPassword)) {
                performSignup(email, username, password)
            }
        }

        binding.loginTextView.setOnClickListener {
            val action = SignupFragmentDirections.actionSignupFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.termsTextView.setOnClickListener {
            safeShowToast("Terms and Privacy not implemented yet")
        }
    }

    private fun validateInputs(email: String, username: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        // Clear previous errors
        binding.emailEditText.error = null
        binding.usernameEditText.error = null
        binding.passwordEditText.error = null
        binding.confirmPasswordEditText.error = null

        // Email validation
        when {
            email.isEmpty() -> {
                binding.emailEditText.error = "Email cannot be empty"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailEditText.error = "Invalid email format"
                isValid = false
            }
        }

        // Username validation
        when {
            username.isEmpty() -> {
                binding.usernameEditText.error = "Username cannot be empty"
                isValid = false
            }
            username.length < 3 -> {
                binding.usernameEditText.error = "Username must be at least 3 characters"
                isValid = false
            }
        }

        // Password validation
        when {
            password.isEmpty() -> {
                binding.passwordEditText.error = "Password cannot be empty"
                isValid = false
            }
            password.length < 6 -> {
                binding.passwordEditText.error = "Password must be at least 6 characters"
                isValid = false
            }
        }

        // Confirm password validation
        when {
            confirmPassword.isEmpty() -> {
                binding.confirmPasswordEditText.error = "Please confirm your password"
                isValid = false
            }
            password != confirmPassword -> {
                binding.confirmPasswordEditText.error = "Passwords do not match"
                isValid = false
            }
        }

        return isValid
    }

    private fun performSignup(email: String, username: String, password: String) {
        signupJob?.cancel()
        toggleLoadingState(true)

        signupJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(1000) // Remove this in production
                if (!isAdded) return@launch

                val response = ApiClient.authApi.signup(SignupRequest(email, username, password))
                if (!isAdded) return@launch

                if (response.status == 200) {
                    clearFields()
                    safeShowToast(getString(R.string.signup_successful))
                    val action = SignupFragmentDirections.actionSignupFragmentToLoginFragment()
                    findNavController().navigate(action)
                } else {
                    response.message?.let { safeShowToast(it) }
                }
            } catch (e: Exception) {
                handleSignupError(e)
            } finally {
                toggleLoadingState(false)
            }
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            binding.signupButton.isEnabled = !isLoading
            binding.emailEditText.isEnabled = !isLoading
            binding.usernameEditText.isEnabled = !isLoading
            binding.passwordEditText.isEnabled = !isLoading
            binding.confirmPasswordEditText.isEnabled = !isLoading
            binding.loginTextView.isEnabled = !isLoading
            binding.termsTextView.isEnabled = !isLoading
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val json = JSONObject(errorBody ?: return "Unknown error")
            json.optString("message", "Unknown error")
        } catch (e: Exception) {
            "Failed to parse error"
        }
    }

    private fun handleSignupError(error: Exception) {
        if (!isAdded) return

        val errorMessage = when {
            error is HttpException -> {
                val raw = error.response()?.errorBody()?.string()
                parseErrorMessage(raw)
            }
            error.message?.contains("timeout", true) == true -> "Connection timeout. Try again."
            error.message?.contains("Unable to resolve host", true) == true -> "No internet connection."
            else -> "Signup failed: ${error.localizedMessage ?: "Unknown error"}"
        }

        safeShowToast(errorMessage)
    }

    private fun safeShowToast(message: String) {
        context?.takeIf { isAdded }?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
            isPasswordVisible = false
        } else {
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0)
            isPasswordVisible = true
        }
        // Move cursor to end of text
        binding.passwordEditText.setSelection(binding.passwordEditText.text?.length ?: 0)
    }

    private fun toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            binding.confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.confirmPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
            isConfirmPasswordVisible = false
        } else {
            binding.confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.confirmPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0)
            isConfirmPasswordVisible = true
        }
        // Move cursor to end of text
        binding.confirmPasswordEditText.setSelection(binding.confirmPasswordEditText.text?.length ?: 0)
    }

    private fun setupPasswordToggles() {
        // Password field toggle - using improved touch detection from LoginFragment
        binding.passwordEditText.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.passwordEditText.compoundDrawables[2] // Right drawable
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.intrinsicWidth
                    val touchAreaStart = binding.passwordEditText.width - binding.passwordEditText.paddingEnd - drawableWidth

                    if (event.x >= touchAreaStart) {
                        togglePasswordVisibility()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        binding.confirmPasswordEditText.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = binding.confirmPasswordEditText.compoundDrawables[2] // Right drawable
                if (drawableEnd != null) {
                    val drawableWidth = drawableEnd.intrinsicWidth
                    val touchAreaStart = binding.confirmPasswordEditText.width - binding.confirmPasswordEditText.paddingEnd - drawableWidth

                    if (event.x >= touchAreaStart) {
                        toggleConfirmPasswordVisibility()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun clearFields() {
        binding.emailEditText.text?.clear()
        binding.usernameEditText.text?.clear()
        binding.passwordEditText.text?.clear()
        binding.confirmPasswordEditText.text?.clear()
    }

    override fun onDestroyView() {
        signupJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}