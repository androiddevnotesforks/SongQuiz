package com.aaronfodor.android.songquiz.model

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
) : UtteranceProgressListener(){

    var requestCounter = 0
    var textToSpeechRequestId = System.currentTimeMillis() + requestCounter

    var textToSpeech: TextToSpeech? = null

    var startedCallback: () -> Unit = {}
    var finishedCallback: () -> Unit = {}
    var errorCallback: () -> Unit = {}

    init {
        init()
    }

    /**
     * Initialize text to speech
     * Set text to speech listener
     */
    fun init(){
        stop()
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.let {
                    val currentLanguageISO3 = Locale.getDefault().isO3Language

                    val ttsLanguage = when (currentLanguageISO3.uppercase()) {
                        "GBR" -> {
                            Locale("GBR")
                        }
                        "HUN" -> {
                            Locale("HUN")
                        }
                        // fallback to British English
                        else -> {
                            Locale.UK
                        }
                    }

                    val languageAvailable = it.isLanguageAvailable(ttsLanguage)
                    if(languageAvailable == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                        languageAvailable == TextToSpeech.LANG_AVAILABLE ||
                        languageAvailable == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE){
                        it.language = ttsLanguage
                    }
                    // fallback to British English
                    else{
                        it.language = Locale.UK
                    }
                }
            }
        }
    }

    fun setCallbacks(started: () -> Unit, finished: () -> Unit, error: () -> Unit){
        startedCallback = started
        finishedCallback = finished
        errorCallback = error
    }

    /**
     * Start speaking
     *
     * @param textToSpeech
     **/
    fun speak(textToSpeech: String){
        Log.i(this.javaClass.name, "speak")
        requestCounter++

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