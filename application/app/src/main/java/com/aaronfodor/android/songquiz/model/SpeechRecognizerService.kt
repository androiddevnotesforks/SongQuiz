package com.aaronfodor.android.songquiz.model

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.aaronfodor.android.songquiz.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

/**
 * Injected everywhere as a singleton
 */
@Singleton
class SpeechRecognizerService @Inject constructor(
    @ApplicationContext val context: Context
) : RecognitionListener{

    companion object{
        const val VIBRATION_DURATION = 50L
    }

    var speechRecognizer: SpeechRecognizer? = null
    var languageBCP47 = ""

    var startedCallback: () -> Unit = {}
    var dBResultChangedCallback: (Float) -> Unit = {}
    var partialCallback: (ArrayList<String>) -> Unit = {}
    var resultCallback: (ArrayList<String>) -> Unit = {}
    var errorCallback: (String) -> Unit = {}

    init {
        init(Locale.getDefault().isO3Language)
    }

    /**
     * Initialize speech recognizer
     */
    fun init(languageISO3: String){
        stopListening()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)

        languageBCP47 = when (languageISO3.uppercase()) {
            "GBR" -> {
                "en-GB"
            }
            "HUN" -> {
                "hu-HU"
            }
            // fallback to British English
            else -> {
                "en-GB"
            }
        }
    }

    fun startListening(started: () -> Unit, dBResultChanged: (Float) -> Unit,
                       partialResult: (ArrayList<String>) -> Unit,
                       result: (ArrayList<String>) -> Unit, error: (String) -> Unit){
        Log.i(this.javaClass.name,"startListening")

        // vibrate on start
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))

        startedCallback = started
        dBResultChangedCallback = dBResultChanged
        partialCallback = partialResult
        resultCallback = result
        errorCallback = error

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageBCP47)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            //putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        speechRecognizer?.startListening(recognizerIntent)
    }

    fun stopListening(){
        speechRecognizer?.stopListening()
    }

    /**
     * Called when the end pointer is ready for the user to start speaking.
     *
     * @param params parameters set by the recognition service. Reserved for future use.
     */
    override fun onReadyForSpeech(params: Bundle?) {
        Log.i(this.javaClass.name,"onReadyForSpeech")
    }

    /**
     * The user has started to speak.
     */
    override fun onBeginningOfSpeech() {
        Log.i(this.javaClass.name,"onBeginningOfSpeech")
        startedCallback()
    }

    /**
     * The sound level in the audio stream has changed. There is no guarantee that this method will
     * be called.
     *
     * @param rmsdB the new RMS dB value
     */
    override fun onRmsChanged(rmsdB: Float) {
        Log.i(this.javaClass.name,"onRmsChanged: $rmsdB")
        dBResultChangedCallback(rmsdB)
    }

    /**
     * More sound has been received. The purpose of this function is to allow giving feedback to the
     * user regarding the captured audio. There is no guarantee that this method will be called.
     *
     * @param buffer a buffer containing a sequence of big-endian 16-bit integers representing a
     * single channel audio stream. The sample rate is implementation dependent.
     */
    override fun onBufferReceived(buffer: ByteArray?) {
        Log.i(this.javaClass.name, "onBufferReceived: $buffer")
    }

    /**
     * Called after the user stops speaking.
     **/
    override fun onEndOfSpeech() {
        Log.i(this.javaClass.name,"onEndOfSpeech")
    }

    /**
     * A network or recognition error occurred.
     *
     * @param errorCode code is defined in [SpeechRecognizer]
     */
    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        val hasListeningStopped = hasErrorFinishedListening(errorCode)
        Log.e(this.javaClass.name,"onError: $errorMessage, has listening stopped: $hasListeningStopped")
        if(hasListeningStopped){
            errorCallback(errorMessage)
        }
    }

    private fun hasErrorFinishedListening(errorCode: Int): Boolean{
        return when(errorCode){
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> false
            SpeechRecognizer.ERROR_CLIENT -> false
            SpeechRecognizer.ERROR_AUDIO -> true
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> true
            SpeechRecognizer.ERROR_NETWORK -> true
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> true
            SpeechRecognizer.ERROR_NO_MATCH -> true
            SpeechRecognizer.ERROR_SERVER -> true
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> true
            else -> true
        }
    }

    private fun getErrorText(errorCode: Int): String{
        return when(errorCode){
            SpeechRecognizer.ERROR_AUDIO -> context.getString(R.string.recognition_error_audio)
            SpeechRecognizer.ERROR_CLIENT -> context.getString(R.string.recognition_error_client)
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> context.getString(R.string.recognition_error_permissions)
            SpeechRecognizer.ERROR_NETWORK -> context.getString(R.string.recognition_error_network)
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> context.getString(R.string.recognition_error_network_timeout)
            SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.recognition_error_no_match)
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> context.getString(R.string.recognition_error_busy)
            SpeechRecognizer.ERROR_SERVER -> context.getString(R.string.recognition_error_server)
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> context.getString(R.string.recognition_error_speech_timeout)
            else -> context.getString(R.string.recognition_not_understand)
        }
    }

    /**
     * Called when partial recognition results are available. The callback might be called at any
     * time between [.onBeginningOfSpeech] and [.onResults] when partial
     * results are ready. This method may be called zero, one or multiple times for each call to
     * [SpeechRecognizer.startListening], depending on the speech recognition
     * service implementation.  To request partial results, use
     * [RecognizerIntent.EXTRA_PARTIAL_RESULTS]
     *
     * @param partialResults the returned results. To retrieve the results in
     * ArrayList&lt;String&gt; format use [Bundle.getStringArrayList] with
     * [SpeechRecognizer.RESULTS_RECOGNITION] as a parameter
     */
    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
        Log.i("Recognition","onResults : $text")
        partialCallback(text)
    }

    /**
     * Called when recognition results are ready.
     *
     * @param results the recognition results. To retrieve the results in `ArrayList<String>` format use [Bundle.getStringArrayList] with
     * [SpeechRecognizer.RESULTS_RECOGNITION] as a parameter. A float array of
     * confidence values might also be given in [SpeechRecognizer.CONFIDENCE_SCORES].
     */
    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
        Log.i("Recognition","onResults : $text")
        resultCallback(text)
    }

    /**
     * Reserved for adding future events.
     *
     * @param eventType the type of the occurred event
     * @param params a Bundle containing the passed parameters
     */
    override fun onEvent(eventType: Int, params: Bundle?) {}

}