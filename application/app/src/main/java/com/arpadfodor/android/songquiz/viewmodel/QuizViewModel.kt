package com.arpadfodor.android.songquiz.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class UserInputState{
    DISABLED, ENABLED, RECORDING
}

enum class TtsState{
    DISABLED, ENABLED, SPEAKING
}

enum class QuizUiState{
    ERROR_PLAYLIST_LOAD, LOADING, READY_TO_START, PLAY, ERROR_PLAY_SONG, ERROR_SPEAK_TO_USER
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    var quizService: QuizService,
    var textToSpeechService: TextToSpeechService,
    var speechRecognizerService: SpeechRecognizerService,
    var mediaPlayerService: MediaPlayerService
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
     * Quiz state
     */
    val quizUiState: MutableLiveData<QuizUiState> by lazy {
        MutableLiveData<QuizUiState>()
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

    /**
     * Playlist image URI
     */
    val playlistImageUri: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    /**
     * The possibly long-running task to notify the user
     */
    var infoToUserJob: Job? = null

    init {
        clearQuizState()
    }

    fun clearQuizState(){
        info.postValue("")
        recognition.postValue("")
        playlistImageUri.postValue("")
        userInputState.postValue(UserInputState.DISABLED)
        ttsState.postValue(TtsState.ENABLED)
        // discard job emitting remaining information to user
        infoToUserJob?.cancel()
        // reset services
        textToSpeechService.stop()
        speechRecognizerService.stopListening()
        // reset quiz state
        quizService.reset()
    }

    fun setPlaylistById(playlistId: String){
        if(quizService.playlistId == playlistId){
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            quizUiState.postValue(QuizUiState.LOADING)
            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.DISABLED)

            val success = quizService.setPlaylistById(playlistId)

            if(success){
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.ENABLED)
                quizUiState.postValue(QuizUiState.READY_TO_START)
                playlistImageUri.postValue(quizService.getPlaylistUri())
            }
            else{
                quizUiState.postValue(QuizUiState.ERROR_PLAYLIST_LOAD)
            }
        }
    }

    fun infoToUser(clearUserInputText: Boolean = false){

        infoToUserJob = viewModelScope.launch {

            if(quizUiState.value == QuizUiState.READY_TO_START){
                quizUiState.postValue(QuizUiState.PLAY)
            }

            if (userInputState.value == UserInputState.RECORDING) {
                return@launch
            }

            if (clearUserInputText) {
                recognition.postValue("")
            }

            val response = quizService.getCurrentInfo()
            val infoList = response.contents
            val immediateAnswerNeeded = response.immediateAnswerNeeded

            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.SPEAKING)

            for (information in infoList) {

                if(!isActive){
                    return@launch
                }

                when (information.type) {
                    InfoType.SPEECH -> {
                        speakToUser(information.payload)
                    }
                    InfoType.SOUND -> {
                        playSound(information.payload)
                    }
                }

            }

            userInputState.value = UserInputState.ENABLED
            ttsState.value = TtsState.ENABLED

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
                cont.resume(true)
            }

            val error = {
                quizUiState.postValue(QuizUiState.ERROR_SPEAK_TO_USER)
                cont.resume(true)
            }

            textToSpeechService.speak(text, started, finished, error)

        }

    }

    private suspend fun playSound(soundUri: String){

        suspendCoroutine<Boolean?> { cont ->

            val finished = {
                cont.resume(true)
            }
            val error = {
                quizUiState.postValue(QuizUiState.ERROR_PLAY_SONG)
                cont.resume(true)
            }

            mediaPlayerService.play(soundUri, finished, error)

        }

    }

    fun getUserInput(){

        if (ttsState.value == TtsState.SPEAKING) {
            return
        }

        ttsState.postValue(TtsState.DISABLED)
        userInputState.postValue(UserInputState.RECORDING)

        val started = {}

        val partial = { textList: ArrayList<String> ->
            recognition.postValue(textList[0])
        }

        val result = { textList: ArrayList<String> ->
            recognition.postValue(textList[0])
            userInputState.postValue(UserInputState.ENABLED)
            ttsState.postValue(TtsState.ENABLED)

            // update state
            val speakToUserNeeded = quizService.userInput(textList)
            if (speakToUserNeeded) {
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