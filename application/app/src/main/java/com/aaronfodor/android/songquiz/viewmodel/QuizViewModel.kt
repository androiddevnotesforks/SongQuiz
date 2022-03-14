package com.aaronfodor.android.songquiz.viewmodel

import android.app.Activity
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.*
import com.aaronfodor.android.songquiz.model.quiz.*
import com.aaronfodor.android.songquiz.model.quiz.GuessFeedback
import com.aaronfodor.android.songquiz.model.repository.PlaylistsRepository
import com.aaronfodor.android.songquiz.model.repository.TracksRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.*
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
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

enum class AddToFavouritesState{
    HIDDEN, VISIBLE_SONG_NOT_IN_FAVOURITES, VISIBLE_SONG_IN_FAVOURITES
}

enum class AdState{
    SHOW, HIDE
}

enum class QuizNotification{
    EMPTY, LOADING, ERROR_PLAYLIST_LOAD, READY_TO_START, PLAY, ERROR_PLAY_SONG, ERROR_SPEAK_TO_USER, ADDED_TO_FAVOURITES, REMOVED_FROM_FAVOURITES, REWARD_GRANTED, EXIT
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    val quizService: QuizService,
    val textToSpeechService: TextToSpeechService,
    val speechRecognizerService: SpeechRecognizerService,
    val mediaPlayerService: MediaPlayerService,
    val adService: AdvertisementService,
    val loggerService: LoggerService,
    val playlistsRepository: PlaylistsRepository,
    val favouritesRepository: TracksRepository,
    accountService: AccountService
) : AppViewModel(accountService) {

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
     * Ad state
     */
    val adState: MutableLiveData<AdState> by lazy {
        MutableLiveData<AdState>()
    }

    /**
     * Add to favourites state
     */
    val addToFavouritesState: MutableLiveData<AddToFavouritesState> by lazy {
        MutableLiveData<AddToFavouritesState>()
    }

    /**
     * Quiz notification
     */
    val notification: MutableLiveData<QuizNotification> by lazy {
        MutableLiveData<QuizNotification>()
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
     * Song played progress value
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
     * Quiz UI state
     */
    val viewModelQuizStanding: MutableLiveData<ViewModelQuizState> by lazy {
        MutableLiveData<ViewModelQuizState>()
    }

    /**
     * Current guesses
     */
    val currentGuesses: MutableLiveData<List<ViewModelGuessItem>> by lazy {
        MutableLiveData<List<ViewModelGuessItem>>()
    }

    /**
     * Winner feedback
     */
    val endFeedback: MutableLiveData<ViewModelEndFeedback> by lazy {
        MutableLiveData<ViewModelEndFeedback>()
    }

    /**
     * Play song progressbar maximum value
     */
    var numProgressBarSteps = 0

    /**
     * Already added favourite Ids
     */
    private val alreadyAddedFavouriteIds = mutableListOf<String>()

    /**
     * Current track
     */
    var currentTrack: ViewModelTrack? = null

    /**
     * The possibly long-running task to notify the user
     */
    private var infoToUserJob: Job? = null

    /**
     * The possibly long-running task to get user input
     */
    private var getUserInputJob: Job? = null

    init {
        viewModelScope.launch {
            clearState()
        }
    }

    private fun stopActions() = viewModelScope.launch {
        info.value = ""
        recognition.value = ""
        playlistImageUri.value = ""
        userInputState.value = UserInputState.DISABLED
        ttsState.value = TtsState.ENABLED
        notification.value = QuizNotification.EMPTY
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
        trackPlayFinished()
        quizService.setClearQuizState()
        viewModelQuizStanding.postValue(quizService.getQuizState().toViewModelQuizState())
    }

    fun setPlaylistAndSettings(playlistId: String, songDuration: Int, repeatAllowed: Boolean,
                               difficultyCompensation: Boolean, extendedInfoAllowed: Boolean)
    = mustAuthenticatedLaunch {
        setPlaylistByIdAndSettings(playlistId, songDuration, repeatAllowed, difficultyCompensation, extendedInfoAllowed)
    }

    private fun setPlaylistByIdAndSettings(playlistId: String, songDuration: Int, repeatAllowed: Boolean,
                                   difficultyCompensation: Boolean, extendedInfoAllowed: Boolean)
    = viewModelScope.launch(Dispatchers.IO) {
        // set already added favourite Ids
        alreadyAddedFavouriteIds.clear()
        alreadyAddedFavouriteIds.addAll(favouritesRepository.getTracks().map { it.id })

        if(quizService.playlist.id == playlistId){
            // prevent activity to reset view model state every time when onCreate is called
            if(notification.value != QuizNotification.EMPTY){
                return@launch
            }

            quizService.setConfigureQuizState()
            // ready to start
            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.ENABLED)
            notification.postValue(QuizNotification.READY_TO_START)
        }
        else{
            // loading state
            notification.postValue(QuizNotification.LOADING)
            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.DISABLED)

            val playlist = playlistsRepository.downloadPlaylistById(playlistId)

            // successfully downloaded
            if(playlist.id == playlistId){
                quizService.setQuizPlaylistAndSettings(playlist, songDuration, repeatAllowed,
                    difficultyCompensation, extendedInfoAllowed)
                // ready to start
                userInputState.postValue(UserInputState.DISABLED)
                ttsState.postValue(TtsState.ENABLED)
                notification.postValue(QuizNotification.READY_TO_START)
                playlistImageUri.postValue(playlist.imageUri)
                // insert/update playlist in the repository
                playlistsRepository.insertPlaylist(playlist)
            }
            else{
                notification.postValue(QuizNotification.ERROR_PLAYLIST_LOAD)
            }
        }

        viewModelQuizStanding.postValue(quizService.getQuizState().toViewModelQuizState())
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
                notification.postValue(QuizNotification.ERROR_SPEAK_TO_USER)
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
                trackPlayStarted()
                isSuccess = false
            }
            val finished = {
                timer?.cancel()
                timer = null
                trackPlayFinished()
                isSuccess = true
            }
            val error = {
                timer?.cancel()
                timer = null
                trackPlayFinished()
                isSuccess = false
                notification.postValue(QuizNotification.ERROR_PLAY_SONG)
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

            if(notification.value == QuizNotification.READY_TO_START){
                notification.postValue(QuizNotification.PLAY)
            }
            if (userInputState.value == UserInputState.RECORDING || notification.value == QuizNotification.EMPTY) {
                return@launch
            }

            if (clearUserInputText) {
                recognition.postValue("")
            }

            val response = quizService.getCurrentInfo()
            val infoList = response.contents.toMutableList()
            immediateAnswerNeeded = response.immediateAnswerNeeded
            isSuccessful = true

            viewModelQuizStanding.postValue(quizService.getQuizState().toViewModelQuizState())
            userInputState.postValue(UserInputState.DISABLED)
            ttsState.postValue(TtsState.SPEAKING)

            var i = 0
            while(i < infoList.size) {
                val information = infoList[i]
                i++

                if(!isActive){
                    return@launch
                }

                val isCurrentSuccessful = when (information) {
                    is Speech -> {
                        currentGuesses.postValue(listOf())
                        endFeedback.postValue(ViewModelEndFeedback())
                        speakToUser(information.speech)
                    }
                    is SoundURL -> {
                        currentGuesses.postValue(listOf())
                        endFeedback.postValue(ViewModelEndFeedback())
                        playUrlSound(information.url)
                    }
                    is LocalSound -> {
                        playLocalSound(information.fileName)
                    }
                    is GuessFeedback -> {
                        currentGuesses.postValue(information.items.toViewModelGuessItemList())
                        endFeedback.postValue(ViewModelEndFeedback())
                        speakToUser(information.speech)
                        true
                    }
                    is EndFeedback -> {
                        currentGuesses.postValue(listOf())
                        endFeedback.postValue(information.toViewModelEndFeedback())
                        speakToUser(information.speech)
                        true
                    }
                    is Advertisement -> {
                        showAd()
                    }
                    is NotifyGetNextInfo -> {
                        val informationPacket = quizService.getCurrentInfo()
                        infoList.addAll(informationPacket.contents)
                        immediateAnswerNeeded = informationPacket.immediateAnswerNeeded
                        viewModelQuizStanding.postValue(quizService.getQuizState().toViewModelQuizState())
                        true
                    }
                    is ExitRequest -> {
                        currentGuesses.postValue(listOf())
                        endFeedback.postValue(ViewModelEndFeedback())
                        notification.postValue(QuizNotification.EXIT)
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
            if (ttsState.value == TtsState.SPEAKING || notification.value == QuizNotification.EMPTY) {
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
                        else -> {}
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

    fun cancelUserInputJob() = viewModelScope.launch {
        // cancel user input job
        getUserInputJob?.cancel()
        speechRecognizerService.stopListening()
        userInputState.postValue(UserInputState.ENABLED)
        ttsState.postValue(TtsState.ENABLED)
    }

    fun explicitUserInput(text: String) = viewModelScope.launch {
        recognition.postValue(text)
        userInputState.postValue(UserInputState.ENABLED)
        ttsState.postValue(TtsState.ENABLED)
        // update state
        val speakToUserNeeded = quizService.userInput(arrayListOf(text))
        if (speakToUserNeeded) {
            infoToUser()
        }
    }

    private fun trackPlayStarted() = viewModelScope.launch(Dispatchers.IO){
        currentTrack = quizService.getCurrentTrack().toViewModelTrack()
        currentTrack?.let {
            loggerService.logGamePlayTrack(this::class.simpleName, it.id)
            if(alreadyAddedFavouriteIds.contains(it.id)){
                addToFavouritesState.postValue(AddToFavouritesState.VISIBLE_SONG_IN_FAVOURITES)
                favouritesRepository.updateTrack(it.toTrack())
            }
            else{
                addToFavouritesState.postValue(AddToFavouritesState.VISIBLE_SONG_NOT_IN_FAVOURITES)
            }
        }
    }

    private fun trackPlayFinished() = viewModelScope.launch {
        currentTrack = null
        addToFavouritesState.postValue(AddToFavouritesState.HIDDEN)
    }

    fun addCurrentTrackToFavourites() = viewModelScope.launch(Dispatchers.IO){
        currentTrack?.let {
            favouritesRepository.insertTrack(it.toTrack())
            loggerService.logAddTrack(this::class.simpleName, it.id)
            alreadyAddedFavouriteIds.add(it.id)
            addToFavouritesState.postValue(AddToFavouritesState.VISIBLE_SONG_IN_FAVOURITES)
            notification.postValue(QuizNotification.ADDED_TO_FAVOURITES)
        }
    }

    fun removeCurrentTrackFromFavourites() = viewModelScope.launch(Dispatchers.IO){
        currentTrack?.let {
            favouritesRepository.deleteTrackById(it.id)
            loggerService.logDeleteTrack(this::class.simpleName, it.id)
            alreadyAddedFavouriteIds.remove(it.id)
            addToFavouritesState.postValue(AddToFavouritesState.VISIBLE_SONG_NOT_IN_FAVOURITES)
            notification.postValue(QuizNotification.REMOVED_FROM_FAVOURITES)
        }
    }

    var isAdShowStarted = false
    var rewardInfoNeeded = false
    var adFinishedAction = {}
    var adRewardAction: (Int) -> Unit = {}

    private suspend fun showAd() : Boolean {
        var isSuccess = false

        suspendCoroutine<Boolean> { cont ->
            adFinishedAction = {
                isSuccess = true
                isAdShowStarted = false
                if(rewardInfoNeeded){
                    rewardInfoNeeded = false
                    notification.postValue(QuizNotification.REWARD_GRANTED)
                }
                cont.resume(true)
            }

            adRewardAction = { amount: Int ->
                viewModelScope.launch(Dispatchers.IO){
                    quizService.addReward()
                    rewardInfoNeeded = true
                }
                Unit
            }

            // request showing the ad
            adState.postValue(AdState.SHOW)
        }

        return isSuccess
    }

    fun showRewardedInterstitialAd(activity: Activity) = viewModelScope.launch{
        // show the ad, if showing is not started yet
        if(!isAdShowStarted){
            isAdShowStarted = true
            loggerService.logShowRewardedInterstitialAd(this::class.simpleName)
            adService.showRewardedInterstitialAd(activity, finishedAction = adFinishedAction, rewardAction = adRewardAction)
            adFinishedAction = {}
            adRewardAction = {}
        }
    }

}