package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.GravityCompat
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
            setOf(R.id.nav_home, R.id.nav_play, R.id.nav_add, R.id.nav_favourites, R.id.nav_statistics,
                R.id.nav_help, R.id.nav_about, R.id.nav_settings),
            activityDrawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun subscribeViewModel() {}
    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    override fun onboardingDialog(){}

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
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            when (navController.currentDestination?.id) {
                R.id.to_nav_home -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_play -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_add -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_favourites -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_statistics -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_help -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_about -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_settings -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_info_from_home -> {
                    navController.navigate(R.id.to_nav_home, null)
                }
                R.id.nav_info_from_play -> {
                    navController.navigate(R.id.to_nav_play, null)
                }
                R.id.nav_info_from_add_playlists -> {
                    navController.navigate(R.id.to_nav_add, null)
                }
                R.id.nav_info_from_favourites -> {
                    navController.navigate(R.id.to_nav_favourites, null)
                }
                R.id.nav_add_from_play -> {
                    navController.navigate(R.id.to_nav_play, null)
                }
                else -> {
                    super.onBackPressed()
                }
            }
        }
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     **/
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            when (item.itemId) {
                R.id.nav_home -> {
                    navController.navigate(R.id.to_nav_home)
                }
                R.id.nav_play -> {
                    navController.navigate(R.id.to_nav_play)
                }
                R.id.nav_add -> {
                    navController.navigate(R.id.to_nav_add)
                }
                R.id.nav_favourites -> {
                    navController.navigate(R.id.nav_favourites)
                }
                R.id.nav_statistics -> {
                    navController.navigate(R.id.nav_statistics)
                }
                R.id.nav_help -> {
                    navController.navigate(R.id.nav_help)
                }
                R.id.nav_about -> {
                    navController.navigate(R.id.nav_about)
                }
                R.id.nav_settings -> {
                    navController.navigate(R.id.nav_settings)
                }
                else ->{
                    return false
                }
            }
        }
        return true

    }
    
}