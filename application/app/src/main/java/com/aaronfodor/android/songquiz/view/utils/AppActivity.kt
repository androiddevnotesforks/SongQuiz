package com.aaronfodor.android.songquiz.view.utils

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.aaronfodor.android.songquiz.R
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import com.aaronfodor.android.songquiz.viewmodel.utils.AuthNotification
import com.aaronfodor.android.songquiz.viewmodel.utils.ViewModelAccountState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class RequiredPermission(
    val id: String,
    val name: String,
    val explanation: String
)

/**
 * The base activity class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppActivity(private val keepScreenAlive: Boolean) : AppCompatActivity(), AuthRequestModule {

    companion object{
        var systemPermissionDialogShowed = false
    }

    abstract val viewModel: AppViewModel

    abstract var requiredPermissions: List<RequiredPermission>
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
        setScreenFlags()
        subscribeViewModel()
        subscribeViewModelAccountState()
        appearingAnimations()
        boardingCheck()
    }

    override fun onPause() {
        unsubscribeViewModel()
        super.onPause()
    }

    private fun permissionCheck(){

        val requestPermissionLambda: (requiredPermission: RequiredPermission) -> Unit = {
            val requestPermissionDialog = AppDialog(
                this,
                getString(R.string.request_permission_title),
                getString(R.string.request_permission_description, it.name, it.explanation),
                R.drawable.icon_warning
            )
            requestPermissionDialog.setPositiveButton{
                lifecycleScope.launch(Dispatchers.Main) {
                    requestPermissionLauncher.launch(it.id)
                    systemPermissionDialogShowed = true
                }
            }
            requestPermissionDialog.setNegativeButton {
                permissionDialogShowed = false
            }
            requestPermissionDialog.show()
            permissionDialogShowed = true
        }

        val requestPermissionSettingsLambda: (requiredPermission: RequiredPermission) -> Unit = {
            val requestSettingsDialog = AppDialog(
                this,
                getString(R.string.request_permission_settings_title),
                getString(R.string.request_permission_settings_description, it.name, it.explanation),
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
                ContextCompat.checkSelfPermission(this, requiredPermission.id) == PackageManager.PERMISSION_GRANTED -> {
                    // Hooray, permission granted
                }
                // permission rejected
                shouldShowRequestPermissionRationale(requiredPermission.id) -> {
                    if(!permissionDialogShowed && !systemPermissionDialogShowed){
                        requestPermissionLambda(requiredPermission)
                    }
                }
                // permission rejected & don't ask again/device policy prohibits having the permission
                else -> {
                    if(!permissionDialogShowed && !systemPermissionDialogShowed){
                        requestPermissionSettingsLambda(requiredPermission)
                    }
                }
            }
        }

    }

    private fun setScreenFlags(){
        if(keepScreenAlive){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    abstract override fun onBackPressed()

    abstract fun subscribeViewModel()
    abstract fun appearingAnimations()
    abstract fun boardingCheck()

    abstract fun unsubscribeViewModel()

    private fun subscribeViewModelAccountState(){
        // this observer is needed in order to propagate the information to the fragments
        val accountStateObserver = Observer<ViewModelAccountState> { accountState -> }
        viewModel.accountState.observe(this, accountStateObserver)

        val authNotificationObserver = Observer<AuthNotification> { notification ->
            when(notification){
                AuthNotification.AUTH_NEEDED -> {
                    viewModel.authNotification.postValue(AuthNotification.NONE)
                    startAuthentication()
                }
                else -> {}
            }
        }
        viewModel.authNotification.observe(this, authNotificationObserver)
    }

    override var authLauncherStarted = false
    override val authLauncher = registerForActivityResult(AuthRequestContract()){ isAuthSuccess ->
        viewModel.authenticationReturned(isAuthSuccess)
        authLauncherStarted = false
    }

}