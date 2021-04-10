package com.arpadfodor.android.songquiz.model

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injected everywhere as a singleton
 */
@Singleton
class TextToSpeechService  @Inject constructor(
    @ApplicationContext val context: Context
) : UtteranceProgressListener() {

    var requestCounter = 0
    var textToSpeechRequestId = System.currentTimeMillis() + requestCounter

    var textToSpeech: TextToSpeech? = null

    var startedCallback: () -> Unit = {}
    var finishedCallback: () -> Unit = {}
    var errorCallback: () -> Unit = {}

    /**
     * Initialize text to speech
     * Set text to speech listener
     */
    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.UK
            }
        }
    }

    /**
     * Start speaking
     *
     * @param textToSpeech
     **/
    fun speak(textToSpeech: String, started: () -> Unit, finished: () -> Unit, error: () -> Unit){
        Log.i(this.javaClass.name, "speak")
        requestCounter++

        startedCallback = started
        finishedCallback = finished
        errorCallback = error
        this.textToSpeech?.setOnUtteranceProgressListener(this)

        this.textToSpeech?.speak(
            textToSpeech,
            TextToSpeech.QUEUE_FLUSH,
            null,
            (textToSpeechRequestId).toString()
        )
    }

    /**
     * Stop speaking
     **/
    fun stop(){
        if(isSpeaking()){
            Log.i(this.javaClass.name, "stop")
            textToSpeech?.stop()
            onDone(null)
        }
    }

    fun isSpeaking(): Boolean{
        return textToSpeech?.isSpeaking ?: false
    }

    override fun onStart(p0: String?) {
        Log.i(this.javaClass.name, "onStart")
        startedCallback()
    }

    override fun onDone(p0: String?) {
        Log.i(this.javaClass.name, "onDone")
        finishedCallback()
    }

    override fun onError(p0: String?) {
        Log.e(this.javaClass.name, "onError")
        errorCallback()
    }

}