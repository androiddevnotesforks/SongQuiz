package com.arpadfodor.android.songquiz.view.utils

import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

abstract class AppActivity(screenAlive: Boolean) : AppCompatActivity() {

    var keepScreenAlive: Boolean = screenAlive

    override fun onResume() {
        super.onResume()
        setKeepScreenFlag()
        permissionCheck()
        subscribeViewModel()
        appearingAnimations()
    }

    override fun onPause() {
        unsubscribeViewModel()
        super.onPause()
    }

    abstract fun appearingAnimations()
    abstract fun permissionCheck()
    abstract fun subscribeViewModel()
    abstract fun unsubscribeViewModel()

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