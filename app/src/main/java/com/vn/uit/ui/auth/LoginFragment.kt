package com.vn.uit.ui.auth

import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.vn.uit.databinding.FragmentLoginBinding
import com.vn.uit.datastore.UserPreferences
import com.vn.uit.model.AuthRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences
    private var loginJob: Job? = null
    private val TAG = "LoginFragment"
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        setupClickListeners()
        setupPasswordToggle()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        binding.googleButton.setOnClickListener { safeShowToast("Google login not implemented yet") }
        binding.facebookButton.setOnClickListener { safeShowToast("Facebook login not implemented yet") }
        binding.othersButton.setOnClickListener { safeShowToast("Other login options not implemented yet") }
        binding.forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        binding.termsTextView.setOnClickListener { safeShowToast("Terms and Privacy not implemented yet") }
        binding.dontHaveAnAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        binding.usernameEditText.error = null
        binding.passwordEditText.error = null

        when {
            email.isEmpty() -> {
                binding.usernameEditText.error = "Email cannot be empty"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.usernameEditText.error = "Invalid email format"
                isValid = false
            }
        }

        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password cannot be empty"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        loginJob?.cancel()
        toggleLoadingState(true)

        loginJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(1000)
                if (!isAdded) return@launch

                val response = ApiClient.authApi.login(AuthRequest(email, password))
                if (!isAdded) return@launch

                if (response.status == 200 && response.data != null) {
                    safeShowToast(getString(R.string.login_successful))

                    val data = response.data

                    if (binding.rememberMeCheckBox.isChecked) {
                        userPreferences.saveUsername(data.username)
                    }

                    userPreferences.saveUserData(
                        token = data.token,
                        refreshToken = data.refreshToken,
                        username = data.username,
                        email = data.email
                    )
                    userPreferences.saveUserId(data.userId)
                } else {
                    response.message?.let { safeShowToast(it) }
                }
            } catch (e: Exception) {
                handleLoginError(e)
            } finally {
                toggleLoadingState(false)
            }
        }
    }

    private fun toggleLoadingState(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            binding.loginButton.isEnabled = !isLoading
            binding.googleButton.isEnabled = !isLoading
            binding.facebookButton.isEnabled = !isLoading
            binding.othersButton.isEnabled = !isLoading
            binding.usernameEditText.isEnabled = !isLoading
            binding.passwordEditText.isEnabled = !isLoading
            binding.rememberMeCheckBox.isEnabled = !isLoading
            binding.forgotPasswordTextView.isEnabled = !isLoading
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

    private fun handleLoginError(error: Exception) {
        if (!isAdded) return

        val errorMessage = when {
            error is HttpException -> {
                val raw = error.response()?.errorBody()?.string()
                parseErrorMessage(raw)
            }
            error.message?.contains("timeout", true) == true -> "Connection timeout. Try again."
            error.message?.contains("Unable to resolve host", true) == true -> "No internet connection."
            else -> "Login failed: ${error.localizedMessage ?: "Unknown error"}"
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
            // Hide password
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0)
            isPasswordVisible = false
        } else {
            // Show password
            binding.passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0)
            isPasswordVisible = true
        }

        // Move cursor to end of text
        binding.passwordEditText.setSelection(binding.passwordEditText.text?.length ?: 0)
    }

    private fun setupPasswordToggle() {
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
    }

    override fun onDestroyView() {
        loginJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}