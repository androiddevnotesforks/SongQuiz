package com.arpadfodor.android.songquiz.view.utils

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

abstract class AppActivity(screenAlive: Boolean) : AppCompatActivity() {

    var keepScreenAlive: Boolean = screenAlive

    override fun onResume() {
        super.onResume()
        subscribeUI()
        setKeepScreenFlag()
        appearingAnimations()
    }

    override fun onPause() {
        unsubscribeUI()
        super.onPause()
    }

    abstract fun appearingAnimations()
    abstract fun subscribeUI()
    abstract fun unsubscribeUI()

    private fun setKeepScreenFlag(){
        if(keepScreenAlive){
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    abstract override fun onBackPressed()

}