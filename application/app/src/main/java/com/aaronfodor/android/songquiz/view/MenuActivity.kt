package com.aaronfodor.android.songquiz.view

import android.Manifest
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.databinding.ActivityMenuBinding
import com.aaronfodor.android.songquiz.view.utils.AppActivityMenu

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
        // Each menu Id as a set of Ids - each should be considered as a top level destination, where the back button is not shown
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_play, R.id.nav_favourites, R.id.nav_statistics, R.id.nav_help, R.id.nav_about, R.id.nav_settings),
            activityDrawerLayout)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupNavigationMenu(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupNavigationMenu(navController: NavController) {
        val sideNavView = binding.menu.navMenu
        sideNavView.setupWithNavController(navController)
    }

    override fun subscribeViewModel() {}
    override fun appearingAnimations() {}
    override fun unsubscribeViewModel() {}

    override fun onboardingDialog(){}

    override fun onResume() {
        super.onResume()
        // prevent Glide using too much memory
        //Glide.get(this).setMemoryCategory(MemoryCategory.LOW)
    }

    override fun onPause() {
        super.onPause()
        // reset Glide's original memory cache strategy
        //Glide.get(this).setMemoryCategory(MemoryCategory.NORMAL)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    // to handle back button specifically
    override fun onBackPressed() {
        when {
            activityDrawerLayout.isDrawerOpen(GravityCompat.START) -> {
                activityDrawerLayout.closeDrawer(GravityCompat.START)
            }
            navController.currentDestination?.id == R.id.nav_home -> {
                // exit dialog
                super.onBackPressed()
            }
            navController.currentDestination?.id == R.id.nav_play -> {
                navController.navigate(R.id.nav_home)
            }
            navController.currentDestination?.id == R.id.nav_favourites -> {
                navController.navigate(R.id.nav_home)
            }
            navController.currentDestination?.id == R.id.nav_statistics -> {
                navController.navigate(R.id.nav_home)
            }
            navController.currentDestination?.id == R.id.nav_help -> {
                navController.navigate(R.id.nav_home)
            }
            navController.currentDestination?.id == R.id.nav_about -> {
                navController.navigate(R.id.nav_home)
            }
            navController.currentDestination?.id == R.id.nav_settings -> {
                navController.navigate(R.id.nav_home)
            }
            else -> {
                navController.navigateUp(appBarConfiguration)
            }
        }
    }

}