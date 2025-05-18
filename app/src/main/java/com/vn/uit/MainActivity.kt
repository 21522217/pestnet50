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

        // Set up toolbar as actionbar
        setSupportActionBar(binding.toolbar)

        userPrefs = UserPreferences(this)
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
            configureAppBar(graphRes)
            configureDrawer()
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
                        configureAppBar(graphRes)
                        configureDrawer()
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
            val authScreens = setOf(R.id.loginFragment, R.id.signupFragment)
            val cameraScreens = setOf(
                R.id.cameraFragment, R.id.imageEditorFragment, R.id.resultFragment
            )

            val isAuthScreen = dest.id in authScreens
            val isCameraScreen = dest.id in cameraScreens

            // Handle drawer visibility and lock state
            binding.drawerLayout.setDrawerLockMode(
                if (isAuthScreen || isCameraScreen) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else DrawerLayout.LOCK_MODE_UNLOCKED
            )
            binding.navView.isVisible = !isAuthScreen

            // Handle toolbar visibility - hide on camera screens
            binding.appBarLayout.isVisible = !isCameraScreen

            // Set system UI flags for camera screens (immersive mode)
            if (isCameraScreen) {
                // Apply camera theme for full immersive experience
                setTheme(R.style.Theme_PestClassification_Camera)

                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    private fun configureAppBar(graphRes: Int) {
        if (graphRes == R.navigation.main_nav_graph) {
            // Top-level destinations where hamburger menu should appear
            val topLevelDestinations = setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_settings,
                R.id.nav_history
            )

            appBarConfig = AppBarConfiguration(
                topLevelDestinations,
                binding.drawerLayout
            )
            setSupportActionBar(binding.toolbar)
            setupActionBarWithNavController(nav, appBarConfig)
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.navView.isVisible = true
        } else {
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
            userPrefs.clearUserData()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        nav.navigateUp(appBarConfig) || super.onSupportNavigateUp()
}