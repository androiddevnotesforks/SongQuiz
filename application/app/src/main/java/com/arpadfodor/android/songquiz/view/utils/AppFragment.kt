package com.arpadfodor.android.songquiz.view.utils

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.arpadfodor.android.songquiz.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * The base fragment class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppFragment : Fragment() {

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
                this.requireContext(),
                getString(R.string.request_permission_title),
                getString(R.string.request_permission_description, it),
                R.drawable.icon_warning
            )
            requestPermissionDialog.setPositiveButton{
                requestPermissionLauncher.launch(it)
            }
            requestPermissionDialog.show()
        }

        for(requiredPermission in activityRequiredPermissions){
            when{
                ContextCompat.checkSelfPermission(this.requireContext(), requiredPermission) == PackageManager.PERMISSION_GRANTED -> {
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

    abstract fun subscribeViewModel()
    abstract fun appearingAnimations()
    abstract fun unsubscribeViewModel()

}