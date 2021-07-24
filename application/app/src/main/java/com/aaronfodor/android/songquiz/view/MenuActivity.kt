package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityMenuBinding
import com.aaronfodor.android.songquiz.view.utils.AppActivityMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence

class MenuActivity : AppActivityMenu(keepScreenAlive = false) {

    private lateinit var binding: ActivityMenuBinding
    override lateinit var activityDrawerLayout: DrawerLayout
    override lateinit var appBarConfiguration: AppBarConfiguration
    override lateinit var navController: NavController

    override var requiredPermissions: List<String> = listOf(
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMenuBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.mainToolbar)

        activityDrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.menu.navMenu

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Each menu Id as a set of Ids - each should be considered as a top level destination
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_playlists, R.id.nav_about, R.id.nav_settings),
            activityDrawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun subscribeViewModel() {}
    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    override fun onboardingDialog(){
        val keyOnboardingFlag = getString(R.string.PREF_KEY_ONBOARDING_MENU_SHOWED)
        // get saved info from preferences
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val onboardingFlag = sharedPreferences.getBoolean(keyOnboardingFlag, false)

        if(!onboardingFlag){
            MaterialTapTargetSequence().addPrompt(
                MaterialTapTargetPrompt.Builder(this)
                    .setTarget(binding.mainToolbar.getChildAt(1))
                    .setPrimaryText(getString(R.string.onboarding_menu))
                    .setAnimationInterpolator(FastOutSlowInInterpolator())
                    .setBackgroundColour(getColor(R.color.colorOnboardingBackground))
                    .setFocalColour(getColor(R.color.colorOnboardingFocal))
                    .setPromptStateChangeListener { prompt, state ->
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_DISMISSING) {
                            // persist showed flag to preferences
                            with(sharedPreferences.edit()){
                                remove(keyOnboardingFlag)
                                putBoolean(keyOnboardingFlag, true)
                                apply()
                            }
                        }
                    }
                    .create()
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // prevent Glide using too much memory
        Glide.get(this).setMemoryCategory(MemoryCategory.LOW)
    }

    override fun onPause() {
        super.onPause()
        // reset Glide's original memory cache strategy
        Glide.get(this).setMemoryCategory(MemoryCategory.NORMAL)
    }

    // to handle pop behavior specifically
    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.nav_playlist_add -> {
                navController.navigate(R.id.to_nav_playlists, null)
            }
            R.id.nav_info_from_playlists -> {
                navController.navigate(R.id.to_nav_playlists, null)
            }
            R.id.nav_info_from_add_playlists -> {
                navController.navigate(R.id.to_nav_playlist_add, null)
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    // to handle back button specifically
    override fun onSupportNavigateUp(): Boolean {
        return when (navController.currentDestination?.id) {
            R.id.nav_playlist_add -> {
                navController.navigate(R.id.to_nav_playlists, null)
                true
            }
            R.id.nav_info_from_playlists -> {
                navController.navigate(R.id.to_nav_playlists, null)
                true
            }
            R.id.nav_info_from_add_playlists -> {
                navController.navigate(R.id.to_nav_playlist_add, null)
                true
            }
            else -> {
                super.onSupportNavigateUp()
            }
        }
    }
    
}