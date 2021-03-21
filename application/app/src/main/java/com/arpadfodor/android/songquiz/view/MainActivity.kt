package com.arpadfodor.android.songquiz.view

import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.arpadfodor.android.songquiz.R
import com.arpadfodor.android.songquiz.databinding.ActivityMainBinding
import com.arpadfodor.android.songquiz.view.utils.AppActivityDrawer

class MainActivity() : AppActivityDrawer(screenAlive = false) {

    private lateinit var binding: ActivityMainBinding
    override lateinit var activityDrawerLayout: DrawerLayout
    override lateinit var appBarConfiguration: AppBarConfiguration
    override lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        activityDrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // Each menu Id as a set of Ids - each should be considered as a top level destination
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_playlists, R.id.nav_about), activityDrawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun appearingAnimations() {}
    override fun permissionCheck() {}
    override fun subscribeViewModel() {}
    override fun unsubscribeViewModel() {}

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     **/
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.nav_playlists -> {
                navController.navigate(R.id.to_nav_playlists)
            }

            R.id.nav_about -> {
                navController.navigate(R.id.to_nav_about)
            }

            else ->{
                return false
            }

        }

        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        return true

    }
    
}