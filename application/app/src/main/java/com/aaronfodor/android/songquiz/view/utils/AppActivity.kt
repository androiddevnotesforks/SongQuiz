package com.aaronfodor.android.songquiz.view.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aaronfodor.android.songquiz.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The base activity class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppActivity(private val keepScreenAlive: Boolean) : AppCompatActivity() {

    companion object{
        var systemPermissionDialogShowed = false
    }

    abstract var requiredPermissions: List<String>
    var permissionDialogShowed = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
        permissionDialogShowed = false
        systemPermissionDialogShowed = false
    }

    override fun onResume() {
        super.onResume()
        permissionCheck()
        setKeepScreenFlag()
        subscribeViewModel()
        appearingAnimations()
        onboardingDialog()
    }

    override fun onPause() {
        unsubscribeViewModel()
        super.onPause()
    }

    private fun permissionCheck(){

        val requestPermissionLambda: (requiredPermission: String) -> Unit = {
            val requestPermissionDialog = AppDialog(
                this,
                getString(R.string.request_permission_title),
                getString(R.string.request_permission_description, it),
                R.drawable.icon_warning
            )
            requestPermissionDialog.setPositiveButton{
                lifecycleScope.launch(Dispatchers.Main) {
                    requestPermissionLauncher.launch(it)
                    systemPermissionDialogShowed = true
                }
            }
            requestPermissionDialog.setNegativeButton {
                permissionDialogShowed = false
            }
            requestPermissionDialog.show()
            permissionDialogShowed = true
        }

        val requestSettingsLambda: (requiredPermission: String) -> Unit = {
            val requestSettingsDialog = AppDialog(
                this,
                getString(R.string.request_settings_title),
                getString(R.string.request_settings_description, it),
                R.drawable.icon_warning
            )
            requestSettingsDialog.setPositiveButton{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:$packageName")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                this.applicationContext.startActivity(intent)
                permissionDialogShowed = false
            }
            requestSettingsDialog.setNegativeButton {
                permissionDialogShowed = false
            }
            requestSettingsDialog.show()
            permissionDialogShowed = true
        }

        for(requiredPermission in requiredPermissions){
            when{
                // permission is granted
                ContextCompat.checkSelfPermission(this, requiredPermission) == PackageManager.PERMISSION_GRANTED -> {
                    // Hooray, permission granted
                }
                // permission rejected
                shouldShowRequestPermissionRationale(requiredPermission) -> {
                    if(!permissionDialogShowed && !systemPermissionDialogShowed){
                        requestPermissionLambda(requiredPermission)
                    }
                }
                // permission rejected & don't ask again/device policy prohibits having the permission
                else -> {
                    if(!permissionDialogShowed && !systemPermissionDialogShowed){
                        requestSettingsLambda(requiredPermission)
                    }
                }
            }
        }

    }

    private fun setKeepScreenFlag(){
        if(keepScreenAlive){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    abstract fun subscribeViewModel()
    abstract fun appearingAnimations()
    abstract fun onboardingDialog()
    abstract fun unsubscribeViewModel()

    abstract override fun onBackPressed()

}