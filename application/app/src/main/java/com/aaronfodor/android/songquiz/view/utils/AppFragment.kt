package com.aaronfodor.android.songquiz.view.utils

import android.content.Intent
import androidx.fragment.app.Fragment
import com.aaronfodor.android.songquiz.view.AuthActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * The base fragment class of the app - can be inherited from
 */
@AndroidEntryPoint
abstract class AppFragment(layoutId: Int) : Fragment(layoutId) {

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

    fun authenticate(){
        val intent = Intent(this.requireContext(), AuthActivity::class.java)
        // After auth finished, return to the caller screen by removing the auth screen
        intent.putExtra(AuthActivity.DESTROY_SELF_WHEN_READY_KEY, true)
        startActivity(intent)
    }

}