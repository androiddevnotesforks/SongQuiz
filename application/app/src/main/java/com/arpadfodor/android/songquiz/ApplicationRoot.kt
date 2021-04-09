package com.arpadfodor.android.songquiz

import android.app.Application
import com.arpadfodor.android.songquiz.model.SpeechRecognizerService
import com.arpadfodor.android.songquiz.model.TextToSpeechService

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
        TextToSpeechService.init(applicationContext)
        SpeechRecognizerService.init(applicationContext)
    }

}