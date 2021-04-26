package com.arpadfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.ConversationService
import com.arpadfodor.android.songquiz.model.InfoType
import com.arpadfodor.android.songquiz.model.SpeechRecognizerService
import com.arpadfodor.android.songquiz.model.TextToSpeechService
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

enum class QuizState{
    ERROR_PLAYLIST_LOAD, LOADING, READY_TO_START, PLAY, ERROR_PLAY_SONG, ERROR_SPEAK_TO_USER
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
     * Quiz state
     */
    val quizState: MutableLiveData<QuizState> by lazy {
        MutableLiveData<QuizState>()
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
     * The possibly long-running task to notify the user
     */
    var infoToUserJob: Job? = null

    init {
        clearQuizState()
    }

    fun clearQuizState(){
        info.postValue("")
        recognition.postValue("")
        userInputState.postValue(UserInputState.DISABLED)
        ttsState.postValue(TtsState.ENABLED)
        // discard job emitting remaining information to user
        infoToUserJob?.cancel()
        // reset services
        textToSpeechService.stop()
        speechRecognizerService.stopListening()
        // reset quiz state
        conversationService.reset()
    }

    fun setPlaylistById(playlistId: String){

        if(conversationService.playlistId == playlistId){
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            quizState.postValue(QuizState.LOADING)
            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.DISABLED)

            val success = conversationService.setPlaylistById(playlistId)

            if(success){
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.ENABLED)
                quizState.postValue(QuizState.READY_TO_START)
            }
            else{
                quizState.postValue(QuizState.ERROR_PLAYLIST_LOAD)
            }
        }

    }

    fun infoToUser(clearUserInputText: Boolean = false){

        infoToUserJob = viewModelScope.launch {

            if(quizState.value == QuizState.READY_TO_START){
                quizState.postValue(QuizState.PLAY)
            }

            if (userInputState.value == UserInputState.RECORDING) {
                return@launch
            }

            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.SPEAKING)

            if (clearUserInputText) {
                recognition.postValue("")
            }

            val response = conversationService.getCurrentInfo()
            val infoList = response.contents
            val immediateAnswerNeeded = response.immediateAnswerNeeded

            for (information in infoList) {

                if(!isActive){
                    return@launch
                }

                when (information.type) {
                    InfoType.SPEECH -> {
                        speakToUser(information.payload)
                    }
                    InfoType.SOUND -> {
                        playSong(information.payload)
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
                cont.resume(true)
            }

            val error = {
                quizState.postValue(QuizState.ERROR_SPEAK_TO_USER)
                cont.resume(true)
            }

            textToSpeechService.speak(text, started, finished, error)

        }

    }

    private suspend fun playSong(soundUri: String){

        suspendCoroutine<Boolean?> { cont ->

            val started = {
                info.postValue(soundUri)
            }

            val finished = {
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
                cont.resume(true)
            }

            val error = {
                quizState.postValue(QuizState.ERROR_PLAY_SONG)
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