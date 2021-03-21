package com.arpadfodor.android.songquiz.model

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

object SpeechRecognizerService : RecognitionListener {

    private const val TAG = "SpeechRecognizerService"

    var speechRecognizer: SpeechRecognizer? = null

    var startedCallback: () -> Unit = {}
    var partialCallback: (ArrayList<String>) -> Unit = {}
    var resultCallback: (ArrayList<String>) -> Unit = {}
    var errorCallback: (String) -> Unit = {}

    /**
     * Initialize speech recognizer
     **/
    fun init(context: Context){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(this)
    }

    fun startListening(started: () -> Unit, partialResult: (ArrayList<String>) -> Unit,
                       result: (ArrayList<String>) -> Unit, error: (String) -> Unit){
        Log.i(TAG,"startListening")

        startedCallback = started
        partialCallback = partialResult
        resultCallback = result
        errorCallback = error

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
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
     * Called when the endpointer is ready for the user to start speaking.
     *
     * @param params parameters set by the recognition service. Reserved for future use.
     */
    override fun onReadyForSpeech(params: Bundle?) {
        Log.i(TAG,"onReadyForSpeech")
    }

    /**
     * The user has started to speak.
     */
    override fun onBeginningOfSpeech() {
        Log.i(TAG,"onBeginningOfSpeech")
        startedCallback()
    }

    /**
     * The sound level in the audio stream has changed. There is no guarantee that this method will
     * be called.
     *
     * @param rmsdB the new RMS dB value
     */
    override fun onRmsChanged(rmsdB: Float) {
        Log.i(TAG,"onRmsChanged: $rmsdB")
    }

    /**
     * More sound has been received. The purpose of this function is to allow giving feedback to the
     * user regarding the captured audio. There is no guarantee that this method will be called.
     *
     * @param buffer a buffer containing a sequence of big-endian 16-bit integers representing a
     * single channel audio stream. The sample rate is implementation dependent.
     */
    override fun onBufferReceived(buffer: ByteArray?) {
        Log.i(TAG, "onBufferReceived: $buffer")
    }

    /**
     * Called after the user stops speaking.
     **/
    override fun onEndOfSpeech() {
        Log.i(TAG,"onEndOfSpeech")
    }

    /**
     * A network or recognition error occurred.
     *
     * @param errorCode code is defined in [SpeechRecognizer]
     */
    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.e(TAG,"onError: $errorMessage")
        errorCallback(errorMessage)
    }

    private fun getErrorText(errorCode: Int): String{
        return when(errorCode){
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
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