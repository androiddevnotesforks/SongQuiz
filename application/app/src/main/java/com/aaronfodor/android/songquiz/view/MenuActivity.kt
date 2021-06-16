package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityMenuBinding
import com.aaronfodor.android.songquiz.view.utils.AppActivityMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory

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
        if (navController.currentDestination?.id == R.id.nav_playlist_add) {
            navController.navigate(R.id.to_nav_playlists, null)
        }
        else{
            super.onBackPressed()
        }
    }

    // to handle back button specifically
    override fun onSupportNavigateUp(): Boolean {
        return if (navController.currentDestination?.id == R.id.nav_playlist_add) {
            navController.navigate(R.id.to_nav_playlists, null)
            true
        } else{
            super.onSupportNavigateUp()
        }
    }
    
}