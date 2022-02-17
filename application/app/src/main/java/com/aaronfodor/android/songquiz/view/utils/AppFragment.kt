package com.aaronfodor.android.songquiz.view.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import com.aaronfodor.android.songquiz.viewmodel.utils.AuthNotification
import com.aaronfodor.android.songquiz.viewmodel.utils.ViewModelAccountState
import dagger.hilt.android.AndroidEntryPoint

/**
 * The base fragment class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppFragment(layoutId: Int) : Fragment(layoutId), AuthRequestModule {

    abstract val viewModel: AppViewModel

    override fun onResume() {
        super.onResume()
        subscribeViewModel()
        subscribeViewModelAccountState()
        appearingAnimations()
    }

    override fun onPause() {
        unsubscribeViewModel()
        super.onPause()
    }

    abstract fun subscribeViewModel()
    abstract fun appearingAnimations()
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