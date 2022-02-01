package com.aaronfodor.android.songquiz.viewmodel

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.MediaPlayerService
import com.aaronfodor.android.songquiz.model.SpeechRecognizerService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.quiz.InfoType
import com.aaronfodor.android.songquiz.model.quiz.QuizService
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class UserInputState{
    DISABLED, ENABLED, RECORDING
}

enum class RmsState{
    LEVEL1, LEVEL2, LEVEL3, LEVEL4, LEVEL5, LEVEL6, LEVEL7
}

enum class TtsState{
    DISABLED, ENABLED, SPEAKING
}

enum class QuizUiState{
    EMPTY, LOADING, ERROR_PLAYLIST_LOAD, READY_TO_START, PLAY, ERROR_PLAY_SONG, ERROR_SPEAK_TO_USER, EXIT
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
     * User speech input RMS state
     */
    val rmsState: MutableLiveData<RmsState> by lazy {
        MutableLiveData<RmsState>()
    }
    var lastRMSIconUpdate = 0L
    val rmsIconUpdateMs = 50
    var sumRMS = 0f
    var numRMSItems = 0

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
     * Playlist primary color
     */
    val playlistPrimaryColor: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * Song played percentage
     */
    val songPlayProgressValue: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    /**
     * Song played progress percentage
     */
    val songPlayProgressPercentage: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }

    /**
     * Play song progressbar maximum value
     */
    var numProgressBarSteps = 0

    /**
     * The possibly long-running task to notify the user
     */
    private var infoToUserJob: Job? = null

    /**
     * The possibly long-running task to get user input
     */
    private var getUserInputJob: Job? = null

    init { viewModelScope.launch {
        clearState()
    } }

    private fun stopActions() = viewModelScope.launch {
        info.value = ""
        recognition.value = ""
        playlistImageUri.value = ""
        userInputState.value = UserInputState.DISABLED
        ttsState.value = TtsState.ENABLED
        uiState.value = QuizUiState.EMPTY
        // discard possible long running remaining jobs (emitting/getting information to/from user)
        infoToUserJob?.cancel()
        getUserInputJob?.cancel()
        // reset services
        mediaPlayerService.stop()
        textToSpeechService.stop()
        speechRecognizerService.stopListening()
    }

    fun clearState() = viewModelScope.launch {
        stopActions()
        quizService.clear()
    }

    fun setPlaylistByIdAndSettings(playlistId: String, songDuration: Int, repeatAllowed: Boolean,
                                   difficultyCompensation: Boolean, extendedInfoAllowed: Boolean)
    = viewModelScope.launch(Dispatchers.IO) {

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
                quizService.setQuizPlaylistAndSettings(playlist, songDuration, repeatAllowed,
                    difficultyCompensation, extendedInfoAllowed)
                // ready to start
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.ENABLED)
                uiState.postValue(QuizUiState.READY_TO_START)
                playlistImageUri.postValue(playlist.previewImageUri)
                // update playlist in the repository
                repository.insertPlaylist(playlist)
            }
            else{
                uiState.postValue(QuizUiState.ERROR_PLAYLIST_LOAD)
            }
        }

    }

    private suspend fun speakToUser(text: String) : Boolean{
        var isSuccess = false

        suspendCoroutine<Boolean> { cont ->

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

        var timer: CountDownTimer? = null

        suspendCoroutine<Boolean> { cont ->

            // countdown timer must run on the UI thread
            Handler(Looper.getMainLooper()).post {
                val timeStepMs = 20L
                val msInSec = 1000L
                val playDurationMs = quizService.songDurationSec * msInSec

                timer = object : CountDownTimer(playDurationMs, timeStepMs) {
                    override fun onTick(millisUntilFinished: Long) {
                        val progressPercentage = (playDurationMs - millisUntilFinished.toFloat()) / playDurationMs
                        val progressValue = (progressPercentage * numProgressBarSteps).toInt()
                        songPlayProgressPercentage.postValue(progressPercentage)
                        songPlayProgressValue.postValue(progressValue)

                    }

                    override fun onFinish() {
                        mediaPlayerService.stop()
                        songPlayProgressValue.postValue(0)
                        isSuccess = true
                        cont.resume(true)
                    }
                }
            }

            val started = {
                timer?.start()
                isSuccess = false
            }
            val finished = {
                timer?.cancel()
                timer?.onFinish()
                timer = null
                isSuccess = true
            }
            val error = {
                timer?.cancel()
                timer?.onFinish()
                timer = null
                isSuccess = false
                uiState.postValue(QuizUiState.ERROR_PLAY_SONG)
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
            }

            mediaPlayerService.playUrlSound(soundUri, started, finished, error)
        }

        return isSuccess
    }

    private suspend fun playLocalSound(soundName: String) : Boolean{
        var isSuccess = false

        suspendCoroutine<Boolean> { cont ->

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

    fun infoToUser(clearUserInputText: Boolean = false) : Job = viewModelScope.launch(Dispatchers.Main) {
        var immediateAnswerNeeded = false
        var isSuccessful = false

        infoToUserJob = viewModelScope.launch(Dispatchers.IO) {

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
            immediateAnswerNeeded = response.immediateAnswerNeeded
            isSuccessful = true

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
                    InfoType.EXIT_QUIZ -> {
                        uiState.postValue(QuizUiState.EXIT)
                        true
                    }
                }

                if(!isCurrentSuccessful){
                    isSuccessful = false
                }

            }

            userInputState.postValue(UserInputState.ENABLED)
            ttsState.postValue(TtsState.ENABLED)
        }
        infoToUserJob?.join()

        // immediate user response is expected
        if (immediateAnswerNeeded && isSuccessful) {
            getUserInput()
        }
    }

    fun getUserInput(){
        getUserInputJob = viewModelScope.launch(Dispatchers.Main) {
            if (ttsState.value == TtsState.SPEAKING || uiState.value == QuizUiState.EMPTY) {
                return@launch
            }

            ttsState.postValue(TtsState.DISABLED)
            userInputState.postValue(UserInputState.RECORDING)
            rmsState.postValue(RmsState.LEVEL1)

            val started = {}
            val rmsValue = { rms: Float ->
                val currentTime = System.currentTimeMillis()
                val nextUpdateNeededAt = lastRMSIconUpdate + rmsIconUpdateMs

                if(nextUpdateNeededAt < currentTime){
                    lastRMSIconUpdate = currentTime

                    val avgRMS = sumRMS / numRMSItems
                    Log.d(this.javaClass.name, "Avg RMS: $avgRMS")

                    sumRMS = 0f
                    numRMSItems = 0

                    when (rmsState.value) {
                        RmsState.LEVEL1 -> {
                            if(avgRMS >= 2){
                                rmsState.postValue(RmsState.LEVEL2)
                            }
                        }
                        RmsState.LEVEL2 -> {
                            if(avgRMS >= 2.5){
                                rmsState.postValue(RmsState.LEVEL3)
                            }
                            else if(avgRMS < 1.5){
                                rmsState.postValue(RmsState.LEVEL1)
                            }
                        }
                        RmsState.LEVEL3 -> {
                            if(avgRMS >= 3){
                                rmsState.postValue(RmsState.LEVEL4)
                            }
                            else if(avgRMS < 2){
                                rmsState.postValue(RmsState.LEVEL2)
                            }
                        }
                        RmsState.LEVEL4 -> {
                            if(avgRMS >= 3.5){
                                rmsState.postValue(RmsState.LEVEL5)
                            }
                            else if(avgRMS < 2.5){
                                rmsState.postValue(RmsState.LEVEL3)
                            }
                        }
                        RmsState.LEVEL5 -> {
                            if(avgRMS >= 4){
                                rmsState.postValue(RmsState.LEVEL6)
                            }
                            else if(avgRMS < 3){
                                rmsState.postValue(RmsState.LEVEL4)
                            }
                        }
                        RmsState.LEVEL6 -> {
                            if(avgRMS >= 4.5){
                                rmsState.postValue(RmsState.LEVEL7)
                            }
                            else if(avgRMS < 3.5){
                                rmsState.postValue(RmsState.LEVEL5)
                            }
                        }
                        RmsState.LEVEL7 -> {
                            if(avgRMS < 4){
                                rmsState.postValue(RmsState.LEVEL6)
                            }
                        }
                    }
                }
                else{
                    sumRMS += rms
                    numRMSItems += 1
                }
            }
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
                    infoToUser()
                }
            }
            val error = { errorMessage: String ->
                recognition.postValue(errorMessage)
                userInputState.postValue(UserInputState.ENABLED)
                ttsState.postValue(TtsState.ENABLED)
            }
            speechRecognizerService.startListening(started, rmsValue, partial, result, error)
        }
    }

    fun empty() = viewModelScope.launch {
        uiState.value = QuizUiState.EMPTY
    }

    fun play() = viewModelScope.launch {
        uiState.value = QuizUiState.PLAY
    }

}