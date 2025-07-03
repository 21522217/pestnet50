package com.vn.uit.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vn.uit.R
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.data.remote.ApiResponse
import com.vn.uit.datastore.UserPreferences
import com.vn.uit.model.ReportApplicationRequest
import com.vn.uit.utils.LocaleHelper
import com.vn.uit.utils.ThemeHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Initialize UserPreferences
        userPreferences = UserPreferences(requireContext())

        val notificationPref = findPreference<androidx.preference.CheckBoxPreference>("notifications")
        val languagePref = findPreference<ListPreference>("language")
        val themePref = findPreference<ListPreference>("theme")

        val currentLang = LocaleHelper.getSelectedLanguage(requireContext())
        languagePref?.value = currentLang

        val currentTheme = ThemeHelper.getSelectedTheme(requireContext())
        themePref?.value = currentTheme

        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val selectedLanguage = newValue.toString()
            if (selectedLanguage != currentLang) {
                LocaleHelper.setLocale(requireContext(), selectedLanguage)
                requireActivity().apply {
                    val intent = activity?.packageManager?.getLaunchIntentForPackage(activity?.packageName ?: "")
                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    activity?.finish()
                }
            }
            true
        }

        themePref?.setOnPreferenceChangeListener { _, newValue ->
            val selectedTheme = newValue.toString()
            if (selectedTheme != currentTheme) {
                ThemeHelper.setTheme(selectedTheme)
                activity?.recreate()
            }
            true
        }

        languagePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        themePref?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()

        notificationPref?.setOnPreferenceChangeListener { _, newValue ->
            // Handle notification toggle logic
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fabReport)
        fab?.setOnClickListener {
            showReportDialog()
        }
    }

    private fun showReportDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report, null)
        val etReport = dialogView.findViewById<EditText>(R.id.etReportMessage)
        val btnSend = dialogView.findViewById<Button>(R.id.btnSendReport)

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        btnSend.setOnClickListener {
            val message = etReport.text.toString().trim()
            if (message.isEmpty()) {
                etReport.error = "Please enter a message"
            } else {
                sendReportToApi(message)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun sendReportToApi(message: String) {
        lifecycleScope.launch {
            try {
                // Get user ID from UserPreferences
                val userIdString = userPreferences.getUserId().first()

                if (userIdString.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Convert string to UUID
                val userId = UUID.fromString(userIdString)

                // Create request object
                val request = ReportApplicationRequest(userId, message)

                // Make API call using coroutines
                val response = ApiClient.authApi.reportApplication(request)

                // Handle response
                if (response.status === 200) {
                    Toast.makeText(requireContext(), "Report sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to send report: ${response.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: IllegalArgumentException) {
                // Handle UUID parsing error
                Toast.makeText(requireContext(), "Invalid user ID format", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Handle other errors
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}