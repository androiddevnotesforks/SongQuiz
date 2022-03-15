package com.aaronfodor.android.songquiz

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.aaronfodor.android.songquiz.model.AdvertisementService
import com.aaronfodor.android.songquiz.model.SpeechRecognizerService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApplicationRoot : Application() {

    @Inject
    lateinit var textToSpeechService: TextToSpeechService

    @Inject
    lateinit var speechRecognizerService: SpeechRecognizerService

    @Inject
    lateinit var advertisementService: AdvertisementService

    private val localeChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            textToSpeechService.init()
            speechRecognizerService.init()
        }
    }

    /**
     * This method fires once as well as the constructor, but also application has context here
     **/
    override fun onCreate() {
        super.onCreate()
        // Initialize ad service
        advertisementService.init()
        // Register to system locale change event
        val filter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)
        registerReceiver(localeChangeReceiver, filter)
    }

}