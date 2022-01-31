package com.aaronfodor.android.songquiz.view.utils

import androidx.activity.result.ActivityResultLauncher

interface AuthRequestModule {

    var authLauncherStarted : Boolean

    val authLauncher : ActivityResultLauncher<Unit>

    fun startAuthentication(){
        if(authLauncherStarted){
            return
        }
        authLauncherStarted = true
        authLauncher.launch(Unit)
    }

}