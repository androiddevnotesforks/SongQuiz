package com.arpadfodor.android.songquiz.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arpadfodor.android.songquiz.model.*
import com.arpadfodor.android.songquiz.model.repository.PlaylistsRepository
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
    EMPTY, LOADING, ERROR_PLAYLIST_LOAD, READY_TO_START, PLAY, ERROR_PLAY_SONG, ERROR_SPEAK_TO_USER
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    var quizService: QuizService,
    var textToSpeechService: TextToSpeechService,
    var speechRecognizerService: SpeechRecognizerService,
    var mediaPlayerService: MediaPlayerService,
    var repository: PlaylistsRepository
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
    val uiState: MutableLiveData<QuizUiState> by lazy {
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
     * Song played percentage
     */
    val songPlayProgress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * Play song progressbar maximum value
     */
    var numProgressSteps = 0

    /**
     * The possibly long-running task to notify the user
     */
    var infoToUserJob: Job? = null

    init {
        clearState()
    }

    private fun stopActions(){
        info.postValue("")
        recognition.postValue("")
        playlistImageUri.postValue("")
        userInputState.postValue(UserInputState.DISABLED)
        ttsState.postValue(TtsState.ENABLED)
        uiState.postValue(QuizUiState.EMPTY)
        // discard job emitting remaining information to user
        infoToUserJob?.cancel()
        // reset services
        mediaPlayerService.stop()
        textToSpeechService.stop()
        speechRecognizerService.stopListening()
    }

    fun clearState(){
        stopActions()
        quizService.clear()
    }

    fun setPlaylistById(playlistId: String){

        viewModelScope.launch(Dispatchers.IO) {

            if(quizService.playlist.id == playlistId){
                // prevent activity to reset view model state every time when onCreate is called
                if(uiState.value != QuizUiState.EMPTY){
                    return@launch
                }

                quizService.setStartQuizState()
                // ready to start
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.ENABLED)
                uiState.postValue(QuizUiState.READY_TO_START)
            }
            else{

                // loading state
                uiState.postValue(QuizUiState.LOADING)
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.DISABLED)

            val playlist = repository.downloadPlaylistById(playlistId)

                // successfully downloaded
                if(playlist.id == playlistId){
                    quizService.setQuizPlaylist(playlist)
                    // ready to start
                    userInputState.postValue(UserInputState.DISABLED)
                    ttsState.postValue(TtsState.ENABLED)
                    uiState.postValue(QuizUiState.READY_TO_START)
                    playlistImageUri.postValue(playlist.previewImageUri)
                    // update playlist in the repository
                    repository.updatePlaylist(playlist)
                }
                else{
                    uiState.postValue(QuizUiState.ERROR_PLAYLIST_LOAD)
                }

            }
        }
    }

    private suspend fun speakToUser(text: String) : Boolean{
        var isSuccess = false

        suspendCoroutine<Boolean?> { cont ->

            val started = {
                info.postValue(text)
            }
            val finished = {
                isSuccess = true
                cont.resume(true)
            }
            val error = {
                uiState.postValue(QuizUiState.ERROR_SPEAK_TO_USER)
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
                isSuccess = false
                cont.resume(true)
            }

            textToSpeechService.setCallbacks(started, finished, error)
            textToSpeechService.speak(text)
        }

        return isSuccess
    }

    private suspend fun playUrlSound(soundUri: String) : Boolean{
        var isSuccess = false

        suspendCoroutine<Boolean?> { cont ->

            val timeStepMs = 20L
            val msInSec = 1000L
            val playDurationMs = quizService.songDurationSec * msInSec

            val timer = object : CountDownTimer(playDurationMs, timeStepMs) {
                override fun onTick(millisUntilFinished: Long) {
                    val songDurationPercentage = (((playDurationMs - millisUntilFinished.toFloat()) / playDurationMs) * numProgressSteps).toInt()
                    songPlayProgress.postValue(songDurationPercentage)
                }

                override fun onFinish() {
                    mediaPlayerService.stop()
                    songPlayProgress.postValue(0)
                    isSuccess = true
                    cont.resume(true)
                }
            }

            val finished = {
                isSuccess = true
            }
            val error = {
                timer.cancel()
                timer.onFinish()
                isSuccess = false
                uiState.postValue(QuizUiState.ERROR_PLAY_SONG)
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
            }

            val playStarted = mediaPlayerService.playUrlSound(soundUri, finished, error)
            if(playStarted){
                timer.start()
            }
        }

        return isSuccess
    }

    private suspend fun playLocalSound(soundName: String) : Boolean{
        var isSuccess = false

        suspendCoroutine<Boolean?> { cont ->

            val finished = {
                isSuccess = true
                cont.resume(true)
            }
            val error = {
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
                isSuccess = false
                cont.resume(true)
            }

            mediaPlayerService.playLocalSound(soundName, finished, error)
        }

        return isSuccess
    }

    fun infoToUser(clearUserInputText: Boolean = false){

        infoToUserJob = viewModelScope.launch {

            if(uiState.value == QuizUiState.READY_TO_START){
                uiState.postValue(QuizUiState.PLAY)
            }
            if (userInputState.value == UserInputState.RECORDING || uiState.value == QuizUiState.EMPTY) {
                return@launch
            }

            if (clearUserInputText) {
                recognition.postValue("")
            }

            val response = quizService.getCurrentInfo()
            val infoList = response.contents
            val immediateAnswerNeeded = response.immediateAnswerNeeded
            var isSuccessful = true

            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.SPEAKING)

            for (information in infoList) {

                if(!isActive){
                    return@launch
                }

                val isCurrentSuccessful = when (information.type) {
                    InfoType.SPEECH -> {
                        speakToUser(information.payload)
                    }
                    InfoType.SOUND_URL -> {
                        playUrlSound(information.payload)
                    }
                    InfoType.SOUND_LOCAL_ID -> {
                        playLocalSound(information.payload)
                    }
                }

                if(!isCurrentSuccessful){
                    isSuccessful = false
                }

            }

            userInputState.value = UserInputState.ENABLED
            ttsState.value = TtsState.ENABLED

            // immediate user response is expected
            if (immediateAnswerNeeded && isSuccessful) {
                getUserInput()
            }

        }

    }

    fun getUserInput(){

        if (ttsState.value == TtsState.SPEAKING || uiState.value == QuizUiState.EMPTY) {
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