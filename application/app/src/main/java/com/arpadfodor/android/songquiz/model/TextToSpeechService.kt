package com.arpadfodor.android.songquiz.model

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*

object TextToSpeechService : UtteranceProgressListener() {

    private const val TAG = "TextToSpeechService"
    var requestCounter = 0
    var textToSpeechRequestId = System.currentTimeMillis() + requestCounter

    var textToSpeech: TextToSpeech? = null

    var startedCallback: () -> Unit = {}
    var finishedCallback: () -> Unit = {}
    var errorCallback: () -> Unit = {}

   /**
    * Initialize text to speech
    * Set text to speech listener
    **/
    fun init(context: Context){
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
        Log.i(TAG,"speak")
        requestCounter++

        startedCallback = started
        finishedCallback = finished
        errorCallback = error
        this.textToSpeech?.setOnUtteranceProgressListener(this)

        this.textToSpeech?.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null, (textToSpeechRequestId).toString())
    }

    /**
     * Stop speaking
     **/
    fun stop(){
        if(isSpeaking()){
            Log.i(TAG,"stop")
            textToSpeech?.stop()
            onDone(null)
        }
    }

    fun isSpeaking(): Boolean{
        return textToSpeech?.isSpeaking ?: false
    }

    override fun onStart(p0: String?) {
        Log.i(TAG,"onStart")
        startedCallback()
    }

    override fun onDone(p0: String?) {
        Log.i(TAG,"onDone")
        finishedCallback()
    }

    override fun onError(p0: String?) {
        Log.e(TAG,"onError")
        errorCallback()
    }

}