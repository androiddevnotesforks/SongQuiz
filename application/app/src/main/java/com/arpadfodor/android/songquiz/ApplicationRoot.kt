package com.arpadfodor.android.songquiz

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationRoot : Application() {

    /**
     * This method fires once as well as the constructor, but also application has context here
     **/
    override fun onCreate() {
        super.onCreate()
        // Initialize Ads
        MobileAds.initialize(this)
    }

}