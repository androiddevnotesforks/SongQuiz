package com.aaronfodor.android.songquiz.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aaronfodor.android.songquiz.model.AccountService
import com.aaronfodor.android.songquiz.model.LoggerService
import com.aaronfodor.android.songquiz.model.MediaPlayerService
import com.aaronfodor.android.songquiz.model.TextToSpeechService
import com.aaronfodor.android.songquiz.model.repository.TracksRepository
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.ViewModelTrack
import com.aaronfodor.android.songquiz.viewmodel.dataclasses.toViewModelTrack
import com.aaronfodor.android.songquiz.viewmodel.utils.AppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InfoTrackUiState{
    LOADING, READY, CLOSE
}

enum class InfoTrackUiNotification{
    NONE, ERROR_LOAD, ERROR_DELETE_ITEM, SUCCESS_DELETE_ITEM, ERROR_PLAY_SONG
}

enum class MediaPlayerInfoTrackState{
    PLAYING, STOPPED
}

enum class TtsInfoTrackState{
    ENABLED, SPEAKING
}

enum class InfoTrackScreenCaller{
    UNSPECIFIED, FAVOURITES
}

@HiltViewModel
class InfoTrackViewModel  @Inject constructor(
    val repository: TracksRepository,
    val textToSpeechService: TextToSpeechService,
    val mediaPlayerService: MediaPlayerService,
    val loggerService: LoggerService,
    accountService: AccountService
) : AppViewModel(accountService) {

    var infoScreenCaller = InfoTrackScreenCaller.UNSPECIFIED
        private set
    fun setCaller(text: String){
        infoScreenCaller = InfoTrackScreenCaller.valueOf(text)
    }

    val uiState: MutableLiveData<InfoTrackUiState> by lazy {
        MutableLiveData<InfoTrackUiState>()
    }

    val notification: MutableLiveData<InfoTrackUiNotification> by lazy {
        MutableLiveData<InfoTrackUiNotification>()
    }

    /**
     * Is text to speech currently speaking
     */
    val ttsState: MutableLiveData<TtsInfoTrackState> by lazy {
        MutableLiveData<TtsInfoTrackState>()
    }

    /**
     * Is media player currently playing
     */
    val mediaPlayerState: MutableLiveData<MediaPlayerInfoTrackState> by lazy {
        MutableLiveData<MediaPlayerInfoTrackState>()
    }

    /**
     * Item to show
     */
    val item: MutableLiveData<ViewModelTrack> by lazy {
        MutableLiveData<ViewModelTrack>()
    }

    init {
        viewModelScope.launch {
            if(textToSpeechService.isSpeaking()){
                ttsState.value = TtsInfoTrackState.SPEAKING
            }
            else{
                ttsState.value = TtsInfoTrackState.ENABLED
            }

            if(mediaPlayerService.isPlaying()){
                mediaPlayerState.value = MediaPlayerInfoTrackState.PLAYING
            }
            else{
                mediaPlayerState.value = MediaPlayerInfoTrackState.STOPPED
            }

            subscribeTtsListeners()
            subscribeMediaPlayerListeners()
        }
    }

    fun subscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {
                ttsState.postValue(TtsInfoTrackState.SPEAKING)
            },
            finished = {
                ttsState.postValue(TtsInfoTrackState.ENABLED)
            },
            error = {
                ttsState.postValue(TtsInfoTrackState.ENABLED)
            }
        )

        if(textToSpeechService.isSpeaking()){
            ttsState.postValue(TtsInfoTrackState.SPEAKING)
        }
        else{
            ttsState.postValue(TtsInfoTrackState.ENABLED)
        }
    }

    fun unsubscribeTtsListeners() = viewModelScope.launch {
        textToSpeechService.setCallbacks(
            started = {},
            finished = {},
            error = {}
        )
    }

    fun subscribeMediaPlayerListeners() = viewModelScope.launch {
        mediaPlayerService.setCallbacks(
            started = {
                mediaPlayerState.postValue(MediaPlayerInfoTrackState.PLAYING)
            },
            finished = {
                mediaPlayerState.postValue(MediaPlayerInfoTrackState.STOPPED)
            },
            error = {
                mediaPlayerState.postValue(MediaPlayerInfoTrackState.STOPPED)
            }
        )

        if(mediaPlayerService.isPlaying()){
            mediaPlayerState.postValue(MediaPlayerInfoTrackState.PLAYING)
        }
        else{
            mediaPlayerState.postValue(MediaPlayerInfoTrackState.STOPPED)
        }
    }

    fun unsubscribeMediaPlayerListeners() = viewModelScope.launch {
        mediaPlayerService.setCallbacks(
            started = {},
            finished = {},
            error = {}
        )
    }

    fun speak(text: String) = viewModelScope.launch {
        textToSpeechService.speak(text)
    }

    fun stopSpeaking() = viewModelScope.launch {
        textToSpeechService.stop()
    }

    fun setItemById(trackId: String, forceLoad: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if(item.value?.id == trackId && !forceLoad){
            ready()
            return@launch
        }
        else{
            // loading state
            uiState.postValue(InfoTrackUiState.LOADING)

            val track = repository.getTrackById(trackId)
            // load from disk
            if(track.id == trackId){
                item.postValue(track.toViewModelTrack())
            }
            // cannot load from disk either
            else{
                notification.postValue(InfoTrackUiNotification.ERROR_LOAD)
            }

            uiState.postValue(InfoTrackUiState.READY)
        }
    }

    fun deleteItem() = viewModelScope.launch(Dispatchers.IO) {
        item.value?.let {
            val success = repository.deleteTrackById(it.id)
            loggerService.logDeleteTrack(this::class.simpleName, it.id)
            if(success){
                notification.postValue(InfoTrackUiNotification.SUCCESS_DELETE_ITEM)
            }
            else{
                notification.postValue(InfoTrackUiNotification.ERROR_DELETE_ITEM)
            }

            uiState.postValue(InfoTrackUiState.CLOSE)
        }
    }

    fun ready() = viewModelScope.launch {
        uiState.value = InfoTrackUiState.READY
    }

    fun playSong() = viewModelScope.launch {
        item.value?.let {
            playUrlSound(it.previewUrl)
        }
    }

    fun stopSong() = viewModelScope.launch {
        mediaPlayerService.stop()
    }

    private fun playUrlSound(soundUri: String) : Boolean{
        var isSuccess = false

        val started = {
            isSuccess = false
            mediaPlayerState.postValue(MediaPlayerInfoTrackState.PLAYING)
        }
        val finished = {
            isSuccess = true
            mediaPlayerState.postValue(MediaPlayerInfoTrackState.STOPPED)
        }
        val error = {
            isSuccess = false
            mediaPlayerState.postValue(MediaPlayerInfoTrackState.STOPPED)
            notification.postValue(InfoTrackUiNotification.ERROR_PLAY_SONG)
        }

        mediaPlayerService.playUrlSound(soundUri, started, finished, error)

        return isSuccess
    }

}