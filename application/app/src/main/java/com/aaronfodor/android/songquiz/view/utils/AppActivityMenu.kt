package com.aaronfodor.android.songquiz.view.utils

import android.app.ActivityOptions
import android.content.Intent
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.aaronfodor.android.songquiz.R
import com.google.android.material.navigation.NavigationView

/**
 * The app activity class having a drawer menu
 */
abstract class AppActivityMenu(keepScreenAlive: Boolean) : AppActivity(keepScreenAlive), NavigationView.OnNavigationItemSelectedListener {

    abstract var activityDrawerLayout: DrawerLayout
    abstract var appBarConfiguration: AppBarConfiguration
    abstract var navController: NavController

    override fun onBackPressed() {
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            exitDialog()
        }
    }

    /**
     * Asks for exit confirmation
     */
    private fun exitDialog(){

        val exitDialog = AppDialog(this, getString(R.string.exit_title),
                getString(R.string.exit_dialog), R.drawable.icon_exit)
        exitDialog.setPositiveButton {
            //showing the home screen - app is not visible but running
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
        }
        exitDialog.show()

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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