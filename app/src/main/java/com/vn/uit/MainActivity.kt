package com.vn.uit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.vn.uit.data.remote.ApiClient
import com.vn.uit.databinding.ActivityMainBinding
import com.vn.uit.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userPrefs: UserPreferences
    private lateinit var appBarConfig: AppBarConfiguration
    private lateinit var nav: NavController
    private var currentGraphId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        userPrefs = UserPreferences(this)


        ApiClient.init(this)


        val host =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        nav = host.navController

        setupDestinationChangeListener()

        lifecycleScope.launch {
            initializeNavGraph()
            observeAuthenticationState()
        }
    }

    private suspend fun initializeNavGraph() {
        val token = userPrefs.getToken().first()
        val graphRes = if (token.isNullOrEmpty()) {
            R.navigation.auth_nav_graph
        } else {
            R.navigation.main_nav_graph
        }

        if (currentGraphId != graphRes) {
            currentGraphId = graphRes
            nav.graph = nav.navInflater.inflate(graphRes)
            configureUI(graphRes)
        }
    }

    private fun observeAuthenticationState() {
        lifecycleScope.launch {
            userPrefs.getToken().collect { token ->
                val graphRes = if (token.isNullOrEmpty()) {
                    R.navigation.auth_nav_graph
                } else {
                    R.navigation.main_nav_graph
                }

                if (currentGraphId != graphRes) {
                    try {
                        currentGraphId = graphRes
                        nav.graph = nav.navInflater.inflate(graphRes)
                        configureUI(graphRes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        recreate()
                    }
                }
            }
        }
    }

    private fun setupDestinationChangeListener() {
        nav.addOnDestinationChangedListener { _, dest, _ ->
            // Define screens that need special handling
            val cameraScreens = setOf(
                R.id.cameraFragment, R.id.imageEditorFragment, R.id.resultFragment
            )

            val isCameraScreen = dest.id in cameraScreens

            // Handle camera screens special UI treatment
            if (isCameraScreen) {
                // Apply camera theme for full immersive experience
                setTheme(R.style.Theme_PestClassification_Camera)

                // Hide app bar for camera screens
                binding.appBarLayout.isVisible = false

                // Set immersive mode for camera screens
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            } else {
                // For non-camera screens, restore normal UI visibility
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

                // Show app bar for non-camera screens in main flow
                if (currentGraphId == R.navigation.main_nav_graph) {
                    binding.appBarLayout.isVisible = true
                }
            }
        }
    }

    private fun configureUI(graphRes: Int) {
        if (graphRes == R.navigation.main_nav_graph) {
            // Configure for main navigation graph (authenticated user)

            // Show app bar and drawer
            binding.appBarLayout.isVisible = true
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.navView.isVisible = true

            // Set up top-level destinations for main flow
            val topLevelDestinations = setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_settings,
                R.id.nav_history,
                R.id.nav_pests
            )

            // Configure app bar with drawer for main flow
            appBarConfig = AppBarConfiguration(
                topLevelDestinations,
                binding.drawerLayout
            )
            setSupportActionBar(binding.toolbar)
            setupActionBarWithNavController(nav, appBarConfig)

            // Configure drawer navigation
            configureDrawer()

        } else {
            // Configure for auth navigation graph (unauthenticated user)

            // Hide app bar and drawer for auth flow
            binding.appBarLayout.isVisible = false
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            binding.navView.isVisible = false
        }
    }

    private fun configureDrawer() {
        binding.navView.setupWithNavController(nav)
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    logoutUser()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, nav)
                    binding.drawerLayout.closeDrawers()
                    true
                }
            }
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            userPrefs.clear()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        nav.navigateUp(appBarConfig) || super.onSupportNavigateUp()
}