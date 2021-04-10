package com.arpadfodor.android.songquiz

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationRoot : Application() {

    companion object{

        // to get unique request permission codes
        var permissionRequestCode = 1
            get() {
                field++
                return field
            }

    }

    /**
     * This method fires once as well as the constructor, but also application has context here
     **/
    override fun onCreate() {
        super.onCreate()
    }

}