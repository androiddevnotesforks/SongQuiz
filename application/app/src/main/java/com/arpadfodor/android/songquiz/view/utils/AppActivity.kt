package com.arpadfodor.android.songquiz.view.utils

import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.arpadfodor.android.songquiz.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The base activity class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppActivity(screenAlive: Boolean) : AppCompatActivity() {

    var keepScreenAlive: Boolean = screenAlive
    abstract var activityRequiredPermissions: List<String>

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted
        } else {
            // Permission denied
        }
    }

    override fun onResume() {
        super.onResume()
        permissionCheck()
        setKeepScreenFlag()
        subscribeViewModel()
        appearingAnimations()
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
                }
            }
            requestPermissionDialog.show()
        }

        for(requiredPermission in activityRequiredPermissions){
            when{
                ContextCompat.checkSelfPermission(this, requiredPermission) == PackageManager.PERMISSION_GRANTED -> {
                    // permission granted
                }
                shouldShowRequestPermissionRationale(requiredPermission) -> {
                    requestPermissionLambda(requiredPermission)
                }
                else -> {
                    requestPermissionLambda(requiredPermission)
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
    abstract fun unsubscribeViewModel()

    abstract override fun onBackPressed()

}