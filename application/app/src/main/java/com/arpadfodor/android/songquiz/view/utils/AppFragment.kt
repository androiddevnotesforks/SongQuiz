package com.arpadfodor.android.songquiz.view.utils

import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * The base fragment class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppFragment : Fragment() {

    override fun onResume() {
        super.onResume()
        subscribeViewModel()
        appearingAnimations()
    }

    override fun onPause() {
        unsubscribeViewModel()
        super.onPause()
    }

    abstract fun subscribeViewModel()
    abstract fun appearingAnimations()
    abstract fun unsubscribeViewModel()

}