package com.vn.uit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.vn.uit.databinding.ActivityMainBinding
import com.vn.uit.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPreferences
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPrefs = UserPreferences(this)

        // Initialize NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController

        // Setup AppBar with drawer
        setSupportActionBar(binding.appBarMain.toolbar)
        setupNavigation()

        // Handle authentication state
        lifecycleScope.launch {
            setupAuthNavigation()
        }
    }

    private suspend fun setupAuthNavigation() {
        // Get token first before setting up navigation
        val token = userPrefs.getToken().first()

        // Set up the appropriate navigation graph based on authentication state
        val graph = navController.navInflater.inflate(
            if (token.isNullOrEmpty()) R.navigation.auth_nav_graph
            else R.navigation.main_nav_graph
        )
        navController.graph = graph

        // Configure listeners based on destination
        setupDestinationListener()

        // Observe authentication changes
        lifecycleScope.launch {
            userPrefs.getToken().collect { token ->
                if (token.isNullOrEmpty() && navController.currentDestination?.id != R.id.loginFragment) {
                    // User logged out, navigate to login
                    navController.navigate(R.id.loginFragment)
                }
            }
        }
    }

    private fun setupNavigation() {
        // Define top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow
            ),
            binding.drawerLayout
        )

        // Link toolbar with navController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup navigation drawer
        binding.navView.setupWithNavController(navController)

        // Handle logout and navigation item clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logoutUser()
                    true
                }
                else -> {
                    // Use the NavigationUI for standard navigation
                    NavigationUI.onNavDestinationSelected(menuItem, navController)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun setupDestinationListener() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Check if current destination is an auth screen or camera screen
            val isAuthScreen = destination.id == R.id.loginFragment || destination.id == R.id.signupFragment
            val isCameraFlow = destination.id == R.id.cameraFragment ||
                    destination.id == R.id.imageEditorFragment ||
                    destination.id == R.id.resultFragment

            // Lock drawer and hide UI elements for certain screens
            binding.drawerLayout.setDrawerLockMode(
                if (isAuthScreen || isCameraFlow) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else DrawerLayout.LOCK_MODE_UNLOCKED
            )

            // Hide the navigation UI for auth screens and camera flow
            binding.navView.isVisible = !isAuthScreen
            binding.appBarMain.toolbar.isVisible = !isAuthScreen && !isCameraFlow
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userPrefs.clearToken()
            // Navigation to login will be triggered by the token collector
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}