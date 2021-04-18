package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.ConversationService
import com.arpadfodor.android.songquiz.model.InfoType
import com.arpadfodor.android.songquiz.model.SpeechRecognizerService
import com.arpadfodor.android.songquiz.model.TextToSpeechService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class UserInputState{
    DISABLED, ENABLED, RECORDING
}

enum class TtsState{
    DISABLED, ENABLED, SPEAKING
}

enum class PlaylistState{
    ERROR, LOADING, READY
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    var conversationService: ConversationService,
    var textToSpeechService: TextToSpeechService,
    var speechRecognizerService: SpeechRecognizerService
) : ViewModel() {

    /**
     * User speech input current state
     */
    val userInputState: MutableLiveData<UserInputState> by lazy {
        MutableLiveData<UserInputState>()
    }

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsState> by lazy {
        MutableLiveData<TtsState>()
    }

    /**
     * Is playlist download finished
     */
    val playlistState: MutableLiveData<PlaylistState> by lazy {
        MutableLiveData<PlaylistState>()
    }

    /**
     * Number of tts speak button presses
     */
    val numListening: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * Last info towards the user
     */
    val info: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    /**
     * Last recognition
     */
    val recognition: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    init {
        clearQuizState()
    }

    fun clearQuizState(){
        userInputState.postValue(UserInputState.ENABLED)
        ttsState.postValue(TtsState.ENABLED)
        numListening.postValue(0)
        recognition.postValue("")
        // reset services
        conversationService.reset()
        textToSpeechService.stop()
        speechRecognizerService.stopListening()
    }

    fun setPlaylistById(playlistId: String){

        viewModelScope.launch(Dispatchers.IO) {
            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.DISABLED)
            playlistState.postValue(PlaylistState.LOADING)

            val success = conversationService.setPlaylistById(playlistId)

            if(success){
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.ENABLED)
                playlistState.postValue(PlaylistState.READY)
            }
            else{
                playlistState.postValue(PlaylistState.ERROR)
            }
        }

    }

    fun infoToUser(clearUserInputText: Boolean = false){

        viewModelScope.launch {

            if (userInputState.value == UserInputState.RECORDING) {
                return@launch
            }

            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.SPEAKING)

            if (clearUserInputText) {
                recognition.postValue("")
            }

            val response = conversationService.getCurrentInfo()
            val infoList = response.first
            val immediateAnswerNeeded = response.second

            for (information in infoList) {

                when (information.first) {
                    InfoType.SPEECH -> {
                        speakToUser(information.second)
                    }
                    InfoType.SOUND -> {
                        playSound(information.second)
                    }
                }

            }

            userInputState.postValue(UserInputState.ENABLED)
            ttsState.postValue(TtsState.ENABLED)

            // immediate user response is expected
            if (immediateAnswerNeeded) {
                getUserInput()
            }

        }

    }

    private suspend fun speakToUser(text: String){

        suspendCoroutine<Boolean?> { cont ->

            val started = {
                info.postValue(text)
            }

            val finished = {
                numListening.postValue(numListening.value?.plus(1))
                cont.resume(true)
            }

            val error = {
                cont.resume(true)
            }

            textToSpeechService.speak(text, started, finished, error)

        }

    }

    private suspend fun playSound(soundUri: String){

        suspendCoroutine<Boolean?> { cont ->

            val started = {
                info.postValue(soundUri)
            }

            val finished = {
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
                numListening.postValue(numListening.value?.plus(1))
                cont.resume(true)
            }

            val error = {
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
                cont.resume(true)
            }

            textToSpeechService.speak(soundUri, started, finished, error)

        }

    }

    fun getUserInput(){

        if(ttsState.value == TtsState.SPEAKING){
            return
        }

        ttsState.postValue(TtsState.DISABLED)
        userInputState.postValue(UserInputState.RECORDING)

        val started = {}

        val partial = { textList: ArrayList<String> ->
            recognition.postValue(textList.toString())
        }

        val result = { textList: ArrayList<String> ->
            recognition.postValue(textList.toString())
            userInputState.postValue(UserInputState.ENABLED)
            ttsState.postValue(TtsState.ENABLED)

            // update state
            val speakToUserNeeded = conversationService.userInput(textList)
            if(speakToUserNeeded){
                viewModelScope.launch(Dispatchers.Main) {
                    infoToUser()
                }
            }
        }

        val error = { errorMessage: String ->
            recognition.postValue(errorMessage)
            userInputState.postValue(UserInputState.ENABLED)
            ttsState.postValue(TtsState.ENABLED)
        }

        speechRecognizerService.startListening(started, partial, result, error)

    }

}