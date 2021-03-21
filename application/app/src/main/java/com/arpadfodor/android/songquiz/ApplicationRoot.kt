package com.arpadfodor.android.songquiz

import android.app.Application
import com.arpadfodor.android.songquiz.model.TextToSpeechService

class ApplicationRoot : Application() {

    /**
     * This method fires once as well as the constructor, but also application has context here
     **/
    override fun onCreate() {
        super.onCreate()

        TextToSpeechService.init(applicationContext)
    }

}